package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.AndroidMapDTO;
import info.tomacla.biketeam.api.dto.GarminMapDTO;
import info.tomacla.biketeam.service.ConfigurationService;
import info.tomacla.biketeam.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/maps")
public class MapAPI {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private MapService mapService;

    @Value("${site.url}")
    private String siteUrl;

    @ResponseBody
    @RequestMapping(value = "/android", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AndroidMapDTO> mapsAndroid() {

        return mapService.listMaps(50).stream()
                .map(m -> {
                    AndroidMapDTO dto = new AndroidMapDTO();
                    dto.setId(m.getId());
                    dto.setTitle(m.getName());
                    dto.setDistance(m.getLength());
                    dto.setTags(m.getTags());
                    dto.setNegativeElevation(Math.round(m.getNegativeElevation()));
                    dto.setPositiveElevation(Math.round(m.getPositiveElevation()));
                    dto.setTime(m.getPostedAt().atStartOfDay(configurationService.getTimezone()).toEpochSecond());
                    dto.setType("route");
                    return dto;
                }).collect(Collectors.toList());

    }

    @ResponseBody
    @RequestMapping(value = "/garmin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<GarminMapDTO>> mapsGarmin() {

        return Map.of("tracks", mapService.listMaps(50).stream()
                .map(m -> {
                    GarminMapDTO dto = new GarminMapDTO();
                    dto.setTitle(m.getName());
                    dto.setUrl(siteUrl + "/api/maps/" + m.getId() + "/fit");
                    return dto;
                }).collect(Collectors.toList()));

    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public byte[] getMapGpxFile(@PathVariable("mapId") String mapId) {
        final Optional<Path> gpxFile = mapService.getGpxFile(mapId);
        if (gpxFile.isPresent()) {
            try {
                return Files.readAllBytes(gpxFile.get());
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading gpx : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find gpx : " + mapId);
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/fit", method = RequestMethod.GET, produces = "application/fit")
    public byte[] getFitFile(@PathVariable("mapId") String mapId) {
        final Optional<Path> fitFile = mapService.getFitFile(mapId);
        if (fitFile.isPresent()) {
            try {
                return Files.readAllBytes(fitFile.get());
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading fit : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find fit : " + mapId);
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/image", method = RequestMethod.GET, produces = "image/png")
    public byte[] getMapImage(@PathVariable("mapId") String mapId) {
        final Optional<Path> imageFile = mapService.getImageFile(mapId);
        if (imageFile.isPresent()) {
            try {
                return Files.readAllBytes(imageFile.get());
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading image : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find image : " + mapId);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/{mapId}/image/refresh", method = RequestMethod.GET)
    public void refreshImage(@PathVariable("mapId") String mapId) {
        mapService.generateImage(mapId);
    }

}
