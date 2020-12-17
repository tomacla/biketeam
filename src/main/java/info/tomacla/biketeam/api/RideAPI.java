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
@RequestMapping(value = "/api/rides")
public class RideAPI {

    @Autowired
    private FileService fileService;

    @ResponseBody
    @RequestMapping(value = "/{rideId}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getRideImage(@PathVariable("rideId") String rideId) {
        String imageName = rideId + ".jpg";
        if (fileService.exists(FileRepositories.RIDE_IMAGES, imageName)) {
            try {
                return Files.readAllBytes(fileService.get(FileRepositories.RIDE_IMAGES, imageName));
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading ride image : " + rideId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find ride image : " + rideId);
    }

}
