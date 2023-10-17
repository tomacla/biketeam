package info.tomacla.biketeam.service.image;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.service.file.FileService;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class ImageService {

    private final String fileRepository;

    private final FileService fileService;

    public ImageService(String fileRepository, FileService fileService) {
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    public void save(String teamId, String elementId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            this.delete(teamId, elementId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.storeFile(newImage, fileRepository, teamId, elementId + optionalFileExtension.get().getExtension());
        }
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
