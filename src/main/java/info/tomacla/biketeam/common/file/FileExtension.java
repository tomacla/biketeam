package info.tomacla.biketeam.common.file;

import info.tomacla.biketeam.common.datatype.Strings;
import liquibase.util.file.FilenameUtils;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum FileExtension {

    PNG(".png", new ArrayList<>(), MediaType.IMAGE_PNG_VALUE, "PNG"),
    JPEG(".jpg", List.of(".jpeg"), MediaType.IMAGE_JPEG_VALUE, "JPEG");

    private final String extension;
    private final List<String> otherExtensions;
    private final String mediaType;
    private final String imageIOType;

    FileExtension(String extension, List<String> otherExtensions, String mediaType, String imageIOType) {
        this.extension = extension;
        this.otherExtensions = otherExtensions;
        this.mediaType = mediaType;
        this.imageIOType = imageIOType;
    }

    public static List<FileExtension> byPriority() {
        return List.of(PNG, JPEG);
    }

    public static Optional<FileExtension> findByFileName(String fileName) {
        if (Strings.isBlank(fileName)) {
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

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public List<String> getOtherExtensions() {
        return otherExtensions;
    }

    public String getImageIOType() {
        return imageIOType;
    }
}
