package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping(value = "/api/publications")
public class PublicationAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/{publicationId}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getPublicationImage(@PathVariable("publicationId") String publicationId) {
        String imageName = publicationId + ".jpg";
        if (fileService.exists(FileRepositories.PUBLICATION_IMAGES, imageName)) {
            try {
                return Files.readAllBytes(fileService.get(FileRepositories.PUBLICATION_IMAGES, imageName));
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading publication image : " + publicationId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find publication image : " + publicationId);
    }

}
