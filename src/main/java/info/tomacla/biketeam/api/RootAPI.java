package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api")
public class RootAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/logo", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getLogo() {
        String imageName = "logo";
        Optional<FileExtension> fileExtensionExists = fileService.exists(imageName, FileExtension.byPriority());
        if (fileExtensionExists.isPresent()) {
            try {

                FileExtension extension = fileExtensionExists.get();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", extension.getMediaType());

                return new ResponseEntity<>(
                        Files.readAllBytes(fileService.get(imageName + extension.getExtension())),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading logo", e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find logo");
    }

}
