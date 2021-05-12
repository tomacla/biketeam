package info.tomacla.biketeam.common;

import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public enum FileExtension {

    PNG(".png", MediaType.IMAGE_PNG_VALUE),
    JPEG(".jpg", MediaType.IMAGE_JPEG_VALUE);

    private final String extension;
    private final String mediaType;

    FileExtension(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static List<FileExtension> byPriority() {
        return List.of(PNG, JPEG);
    }

    public static Optional<FileExtension> findByFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        for (FileExtension fileExtension : byPriority()) {
            if (fileName.toLowerCase().endsWith(fileExtension.getExtension())) {
                return Optional.of(fileExtension);
            }
        }
        return Optional.empty();
    }

}
