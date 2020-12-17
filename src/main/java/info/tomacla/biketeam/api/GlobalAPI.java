package info.tomacla.biketeam.api;

import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping(value = "/api")
public class GlobalAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/logo", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getLogo() {
        String imageName = "logo.jpg";
        if (fileService.exists(imageName)) {
            try {
                return Files.readAllBytes(fileService.get(imageName));
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading logo", e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find logo");
    }

}
