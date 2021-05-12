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
@RequestMapping(value = "/api/rides")
public class RideAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/{rideId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRideImage(@PathVariable("rideId") String rideId) {
        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.RIDE_IMAGES, rideId, FileExtension.byPriority());
        if (fileExtensionExists.isPresent()) {
            try {

                FileExtension extension = fileExtensionExists.get();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", extension.getMediaType());

                return new ResponseEntity<>(
                        Files.readAllBytes(fileService.get(FileRepositories.RIDE_IMAGES, rideId + extension.getExtension())),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading ride image : " + rideId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find ride image : " + rideId);

    }

}
