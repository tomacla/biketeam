package info.tomacla.biketeam.service.image;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.file.ThumbnailService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ImageService {

    private final String fileRepository;

    private final FileService fileService;

    private final ThumbnailService thumbnailService;

    public ImageService(String fileRepository, FileService fileService, ThumbnailService thumbnailService) {
        this.fileRepository = fileRepository;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
    }

    public void save(String teamId, String elementId, InputStream is, String fileName) throws IOException {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            FileExtension fileExtension = optionalFileExtension.get();
            this.delete(teamId, elementId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            this.resizeIfNeeded(newImage, fileExtension);
            fileService.storeFile(newImage, fileRepository, teamId, elementId + fileExtension.getExtension());
        }
    }

    private void resizeIfNeeded(Path targetImagePath, FileExtension fileExtension) throws IOException {
        byte[] bytes = Files.readAllBytes(targetImagePath);
        bytes = thumbnailService.resizeImage(bytes, 600, fileExtension);
        Files.write(targetImagePath, bytes);
    }

    public void delete(String teamId, String elementId) {
        get(teamId, elementId).ifPresent(image ->
                fileService.deleteFile(fileRepository, teamId, elementId + image.getExtension().getExtension())
        );
    }

    public Optional<ImageDescriptor> get(String teamId, String elementId) {

        Optional<FileExtension> fileExtensionExists = fileService.fileExists(fileRepository, teamId, elementId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.getFile(fileRepository, teamId, elementId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

}
