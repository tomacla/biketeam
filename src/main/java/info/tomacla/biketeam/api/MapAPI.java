package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.AndroidMapDTO;
import info.tomacla.biketeam.api.dto.GarminMapDTO;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/{teamId}/maps")
public class MapAPI {

    @Autowired
    private TeamService teamService;

    @Autowired
    private MapService mapService;

    @Autowired
    private UrlService urlService;

    @ResponseBody
    @RequestMapping(value = "/android", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AndroidMapDTO> mapsAndroid(@PathVariable("teamId") String teamId) {

        Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team"));

        return mapService.listMaps(teamId, 50).stream()
                .map(m -> {
                    AndroidMapDTO dto = new AndroidMapDTO();
                    dto.setId(m.getId());
                    dto.setTitle(m.getName());
                    dto.setDistance(m.getLength());
                    dto.setTags(m.getTags());
                    dto.setNegativeElevation(Math.round(m.getNegativeElevation()));
                    dto.setPositiveElevation(Math.round(m.getPositiveElevation()));
                    dto.setTime(m.getPostedAt().atStartOfDay(ZoneId.of(team.getConfiguration().getTimezone())).toEpochSecond());
                    dto.setType(m.getType().getLabel().toLowerCase());
                    return dto;
                }).collect(Collectors.toList());

    }

    @ResponseBody
    @RequestMapping(value = "/garmin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<GarminMapDTO>> mapsGarmin(@PathVariable("teamId") String teamId) {

        return Map.of("tracks", mapService.listMaps(teamId, 50).stream()
                .map(m -> {
                    GarminMapDTO dto = new GarminMapDTO();
                    dto.setTitle(m.getName());
                    dto.setUrl(urlService.getMapFitUrl(teamId, m.getId()));
                    return dto;
                }).collect(Collectors.toList()));

    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public byte[] getMapGpxFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> gpxFile = mapService.getGpxFile(teamId, mapId);
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
    public byte[] getFitFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> fitFile = mapService.getFitFile(teamId, mapId);
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
    public byte[] getMapImage(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> imageFile = mapService.getImageFile(teamId, mapId);
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
    public void refreshImage(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        mapService.generateImage(teamId, mapId);
    }

}
