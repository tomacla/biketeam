package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.service.RideService;
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
@RequestMapping(value = "/api/{teamId}/rides")
public class RideAPI {

    @Autowired
    private RideService rideService;

    @ResponseBody
    @RequestMapping(value = "/{rideId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRideImage(@PathVariable("teamId") String teamId, @PathVariable("rideId") String rideId) {
        final Optional<ImageDescriptor> image = rideService.getImage(teamId, rideId);
        if (image.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", image.get().getExtension().getMediaType());

                return new ResponseEntity<>(
                        Files.readAllBytes(image.get().getPath()),
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
