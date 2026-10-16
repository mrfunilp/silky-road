package com.silkyroad.backend.marketplace;

import com.silkyroad.backend.common.NotFoundException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "png", "gif", "webp");
    private static final int MAX_INPUT_DIMENSION = 8000;
    private static final int MAX_OUTPUT_DIMENSION = 1600;
    private static final Pattern STORED_NAME =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)");

    private final Path directory;

    public ImageStorageService(@Value("${silkyroad.images.dir}") String directory) throws IOException {
        ImageIO.scanForPlugins();
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        Files.createDirectories(this.directory);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        BufferedImage image = downscale(decode(file));
        boolean transparent = image.getColorModel().hasAlpha();
        String extension = transparent ? "png" : "jpg";
        String name = UUID.randomUUID() + "." + extension;

        try {
            BufferedImage output = transparent ? image : toRgb(image);
            if (!ImageIO.write(output, transparent ? "png" : "jpeg", directory.resolve(name).toFile())) {
                throw new IllegalStateException("No image writer available for " + extension);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return name;
    }

    public Resource load(String name) {
        Path path = resolve(name);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("No image " + name);
        }
        return new PathResource(path);
    }

    public MediaType mediaType(String name) {
        return name.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
    }

    public void delete(String name) {
        try {
            Files.deleteIfExists(resolve(name));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path resolve(String name) {
        if (name == null || !STORED_NAME.matcher(name).matches()) {
            throw new NotFoundException("No image " + name);
        }
        return directory.resolve(name);
    }

    private BufferedImage decode(MultipartFile file) {
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = input == null ? null : ImageIO.getImageReaders(input);
            if (readers == null || !readers.hasNext()) {
                throw unsupported();
            }
            ImageReader reader = readers.next();
            try {
                if (!ALLOWED_FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw unsupported();
                }
                reader.setInput(input, true, true);
                if (reader.getWidth(0) > MAX_INPUT_DIMENSION || reader.getHeight(0) > MAX_INPUT_DIMENSION) {
                    throw new IllegalArgumentException(
                            "Image is too large. Maximum is " + MAX_INPUT_DIMENSION + " pixels per side.");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw unsupported();
        }
    }

    private BufferedImage downscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int longest = Math.max(width, height);
        if (longest <= MAX_OUTPUT_DIMENSION) {
            return image;
        }

        double scale = (double) MAX_OUTPUT_DIMENSION / longest;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int type = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    private BufferedImage toRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return rgb;
    }

    private IllegalArgumentException unsupported() {
        return new IllegalArgumentException("Unsupported file. Allowed are JPEG, PNG, GIF and WebP images.");
    }
}
