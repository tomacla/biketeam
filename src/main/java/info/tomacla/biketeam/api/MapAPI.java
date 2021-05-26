package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.AndroidMapDTO;
import info.tomacla.biketeam.api.dto.GarminMapDTO;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Gpx;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.service.ConfigurationService;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/maps")
public class MapAPI {

    @Autowired
    private FileService fileService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private MapRepository mapRepository;

    @Value("${site.url}")
    private String siteUrl;

    @ResponseBody
    @RequestMapping(value = "/android", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AndroidMapDTO> mapsAndroid() {

        return mapRepository.findAll(PageRequest.of(0, 50, Sort.by("postedAt").descending())).stream()
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

        return Map.of("tracks", mapRepository.findAll(PageRequest.of(0, 50, Sort.by("postedAt").descending())).stream()
                .map(m -> {
                    GarminMapDTO dto = new GarminMapDTO();
                    dto.setTitle(m.getName());
                    dto.setUrl(siteUrl + "/api/maps/" + m.getId() + "/gpx");
                    return dto;
                }).collect(Collectors.toList()));

    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public byte[] getMapGpxFile(@PathVariable("mapId") String mapId) {
        String gpxName = mapId + ".gpx";
        if (fileService.exists(FileRepositories.GPX_FILES, gpxName)) {
            try {
                return Files.readAllBytes(fileService.get(FileRepositories.GPX_FILES, gpxName));
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading gpx : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find gpx : " + mapId);
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/image", method = RequestMethod.GET, produces = "image/png")
    public byte[] getMapImage(@PathVariable("mapId") String mapId) {
        String mapImage = mapId + ".png";
        if (fileService.exists(FileRepositories.MAP_IMAGES, mapImage)) {
            try {
                return Files.readAllBytes(fileService.get(FileRepositories.MAP_IMAGES, mapImage));
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading image : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find image : " + mapId);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/{mapId}/image/refresh", method = RequestMethod.GET)
    public void refreshImage(@PathVariable("mapId") String mapId) {

        String gpxName = mapId + ".gpx";
        if (fileService.exists(FileRepositories.GPX_FILES, gpxName)) {
            Path staticMapImage =  Gpx.staticImage(fileService.get(FileRepositories.GPX_FILES, gpxName), configurationService.getSiteIntegration().getMapBoxAPIKey());
            fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, mapId + ".png");
        }

    }

}
