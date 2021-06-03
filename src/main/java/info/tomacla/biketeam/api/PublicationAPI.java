package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/publications")
public class PublicationAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/{publicationId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getPublicationImage(@PathVariable("publicationId") String publicationId) {
        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.PUBLICATION_IMAGES, publicationId, FileExtension.byPriority());
        if (fileExtensionExists.isPresent()) {
            try {

                FileExtension extension = fileExtensionExists.get();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", extension.getMediaType());

                return new ResponseEntity<>(
                        Files.readAllBytes(fileService.get(FileRepositories.PUBLICATION_IMAGES, publicationId + extension.getExtension())),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading publication image : " + publicationId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find publication image : " + publicationId);
    }

}
