package com.silkyroad.backend.marketplace;

import java.time.Duration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageStorageService imageStorage;

    public ImageController(ImageStorageService imageStorage) {
        this.imageStorage = imageStorage;
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> get(@PathVariable String name) {
        Resource image = imageStorage.load(name);
        return ResponseEntity.ok()
                .contentType(imageStorage.mediaType(name))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'")
                .body(image);
    }
}
