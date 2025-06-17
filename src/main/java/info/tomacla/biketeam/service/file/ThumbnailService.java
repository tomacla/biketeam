package info.tomacla.biketeam.service.file;

import info.tomacla.biketeam.common.file.FileExtension;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.ImageFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import java.nio.file.Paths;

@Service
public class ThumbnailService {
    private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);

    @Autowired
    private FileService fileService;

    public Path resizeImage(Path originalImage, int targetWidth, FileExtension format) {
        try {
            final byte[] bytes = this.resizeImage(Files.readAllBytes(originalImage), targetWidth, format);
            final Path output = fileService.getTempFile("thumb", format.getExtension());
            Files.write(output, bytes);
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Error while generating image", e);
        }
    }

    public byte[] resizeImage(byte[] originalImage, int targetWidth, FileExtension format) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(originalImage))
                    .scale(1) // do not resize
                    .addFilter(new NoScaleUpResizer(targetWidth, targetWidth)) // then resize only if larger
                    .outputFormat(format.getImageIOType())
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while generating image", e);
        }
    }

    public byte[] getOptimizedImage(Path imagePath, int maxDimension) throws IOException {
        String cacheKey = imagePath.toString() + "_" + maxDimension;
        Path cachedImagePath = getCachedImagePath(cacheKey);

        if (Files.exists(cachedImagePath)) {
            return Files.readAllBytes(cachedImagePath);
        }

        BufferedImage originalImage = ImageIO.read(imagePath.toFile());
        BufferedImage resizedImage = resizeImage(originalImage, maxDimension);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, getImageFormat(imagePath), outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        CompletableFuture.runAsync(() -> {
            try {
                Files.write(cachedImagePath, imageBytes);
            } catch (IOException e) {
                log.error("Failed to cache image {}", e);
            }
        });

        return imageBytes;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxDimension) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
            return originalImage;
        }

        float ratio = (float) originalWidth / originalHeight;
        int targetWidth, targetHeight;

        if (originalWidth > originalHeight) {
            targetWidth = maxDimension;
            targetHeight = Math.round(maxDimension / ratio);
        } else {
            targetHeight = maxDimension;
            targetWidth = Math.round(maxDimension * ratio);
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resizedImage;
    }

    private Path getCachedImagePath(String cacheKey) {
        String hashedKey = DigestUtils.md5Hex(cacheKey);

        return Paths.get(System.getProperty("java.io.tmpdir"), "image_cache", hashedKey);
    }

    private String getImageFormat(Path imagePath) {
        String fileName = imagePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".png")) return "PNG";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "JPEG";
        if (fileName.endsWith(".gif")) return "GIF";

        return "JPEG";
    }

    public class NoScaleUpResizer implements ImageFilter {
        private final int maxWidth;
        private final int maxHeight;

        public NoScaleUpResizer(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        @Override
        public BufferedImage apply(BufferedImage img) {
            if (img.getWidth() <= maxWidth && img.getHeight() <= maxHeight) {
                return img;
            }
            try {
                return Thumbnails.of(img).size(maxWidth, maxHeight).asBufferedImage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
