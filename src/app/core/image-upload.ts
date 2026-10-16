export const MAX_IMAGES_PER_ITEM = 3;
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
export const IMAGE_ACCEPT = '.jpg,.jpeg,.png,.gif,.webp,' + ALLOWED_IMAGE_TYPES.join(',');

const ALLOWED_EXTENSIONS = /\.(jpe?g|png|gif|webp)$/i;

export function imageFileError(file: File): string | null {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type) || !ALLOWED_EXTENSIONS.test(file.name)) {
    return `"${file.name}" is not allowed. Use JPEG, PNG, GIF or WebP.`;
  }
  if (file.size > MAX_IMAGE_BYTES) {
    return `"${file.name}" is larger than 5 MB.`;
  }
  return null;
}
