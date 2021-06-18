package info.tomacla.biketeam.common;

import liquibase.util.file.FilenameUtils;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum FileExtension {

    PNG(".png", new ArrayList<>(), MediaType.IMAGE_PNG_VALUE),
    JPEG(".jpg", List.of(".jpeg"), MediaType.IMAGE_JPEG_VALUE);

    private final String extension;
    private final List<String> otherExtensions;
    private final String mediaType;

    FileExtension(String extension, List<String> otherExtensions, String mediaType) {
        this.extension = extension;
        this.otherExtensions = otherExtensions;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public List<String> getOtherExtensions() {
        return otherExtensions;
    }

    public static List<FileExtension> byPriority() {
        return List.of(PNG, JPEG);
    }

    public static Optional<FileExtension> findByFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }

        final String targetExtension = "." + FilenameUtils.getExtension(fileName).toLowerCase();

        for (FileExtension fileExtension : byPriority()) {
            if (fileExtension.getExtension().equals(targetExtension) || fileExtension.getOtherExtensions().contains(targetExtension)) {
                return Optional.of(fileExtension);
            }
        }
        return Optional.empty();
    }

}
