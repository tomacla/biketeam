package info.tomacla.biketeam.common;

import java.nio.file.Path;
import java.util.Objects;

public class ImageDescriptor {

    private final FileExtension extension;
    private final Path path;

    private ImageDescriptor(FileExtension extension, Path path) {
        this.extension = Objects.requireNonNull(extension);
        this.path = Objects.requireNonNull(path);
    }

    public static ImageDescriptor of(FileExtension extension, Path path) {
        return new ImageDescriptor(extension, path);
    }

    public FileExtension getExtension() {
        return extension;
    }

    public Path getPath() {
        return path;
    }
}
