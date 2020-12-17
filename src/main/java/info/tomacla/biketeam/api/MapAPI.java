package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.domain.navigationmap.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.domain.navigationmap.MapRepository;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/maps")
public class MapAPI {

    @Autowired
    private FileService fileService;

    @Autowired
    private MapRepository mapRepository;

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

    @ResponseBody
    @RequestMapping(value = "/autocomplete/tags", method = RequestMethod.GET)
    public List<String> getTags(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) {
            return mapRepository.findAllDistinctTags();
        }
        return mapRepository.findDistinctTagsContainer(q.toLowerCase());
    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public Map<String, String> listMaps(@RequestParam("q") String q) {
        List<MapIdNamePostedAtVisibleProjection> maps = q == null || q.isBlank() ? mapRepository.findAllByOrderByPostedAtDesc()
                : mapRepository.findAllByNameContainingIgnoreCaseOrderByPostedAtDesc(q);
        return maps.stream().collect(Collectors.toMap(MapIdNamePostedAtVisibleProjection::getId, MapIdNamePostedAtVisibleProjection::getName));

    }

}
