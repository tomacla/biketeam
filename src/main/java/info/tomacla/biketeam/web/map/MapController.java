package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.url.UrlService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.ride.dto.AndroidMapDTO;
import info.tomacla.biketeam.web.ride.dto.GarminMapDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}/maps")
public class MapController extends AbstractController {

    @Autowired
    private MapService mapService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Value("${mapbox.api-key}")
    private String mapBoxAPIKey;

    @GetMapping(value = "/{mapId}")
    public String getMap(@PathVariable("teamId") String teamId,
                         @PathVariable("mapId") String mapId,
                         @ModelAttribute("error") String error,
                         @RequestParam(required = false, defaultValue = "false") boolean embed,
                         Principal principal,
                         Model model) {

        final Team team = checkTeam(teamId);

        Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
        if (optionalMap.isEmpty()) {
            return viewHandler.redirect(team, "/maps");
        }

        Map map = optionalMap.get();
        addOpenGraphValues(team,
                model,
                map.getName(),
                urlService.getMapImageUrl(team, map),
                urlService.getMapUrl(team, map),
                getMapOGDescription(map)
        );

        addGlobalValues(principal, model, "Map " + map.getName(), team);
        model.addAttribute("map", map);
        model.addAttribute("mapBoxAPIKey", mapBoxAPIKey);
        model.addAttribute("_embed", embed);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "map";

    }

    @GetMapping
    public String getMaps(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "lowerDistance", required = false, defaultValue = "1") double lowerDistance,
                          @RequestParam(value = "upperDistance", required = false, defaultValue = "1000") double upperDistance,
                          @RequestParam(value = "lowerPositiveElevation", required = false, defaultValue = "0") double lowerPositiveElevation,
                          @RequestParam(value = "upperPositiveElevation", required = false, defaultValue = "3000") double upperPositiveElevation,
                          @RequestParam(value = "sort", required = false) MapSorterOption sort,
                          @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                          @RequestParam(value = "type", required = false) MapType type,
                          @RequestParam(value = "tags", required = false) List<String> tags,
                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "pageSize", defaultValue = "9", required = false) int pageSize,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        SearchMapForm form = SearchMapForm.builder()
                .withTags(tags == null ? team.getConfiguration().getDefaultSearchTags() : tags)
                .withSort(sort)
                .withWindDirection(windDirection)
                .withLowerDistance(lowerDistance)
                .withUpperDistance(upperDistance)
                .withLowerPositiveElevation(lowerPositiveElevation)
                .withUpperPositiveElevation(upperPositiveElevation)
                .withPage(page)
                .withPageSize(pageSize)
                .withType(type)
                .get();

        SearchMapForm.SearchMapFormParser parser = form.parser();

        Page<Map> maps = mapService.searchMaps(
                team.getId(),
                parser.getPage(),
                parser.getPageSize(),
                parser.getSort(),
                parser.getLowerDistance(),
                parser.getUpperDistance(),
                parser.getType(),
                parser.getLowerPositiveElevation(),
                parser.getUpperPositiveElevation(),
                parser.getTags(),
                parser.getWindDirection());

        addGlobalValues(principal, model, "Maps", team);
        model.addAttribute("maps", maps.getContent());
        model.addAttribute("pages", maps.getTotalPages());
        model.addAttribute("tags", mapService.listTags(team.getId()));
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "maps";
    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public java.util.Map<String, String> autocompleteMaps(@PathVariable("teamId") String teamId,
                                                          @RequestParam("q") String q) {
        return mapService.searchMaps(teamId, q)
                .stream()
                .collect(Collectors.toMap(MapProjection::getId, MapProjection::getName));

    }

    @ResponseBody
    @RequestMapping(value = "/android", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AndroidMapDTO> mapsAndroid(@PathVariable("teamId") String teamId) {

        Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team"));

        return mapService.listMaps(teamId, 50).stream()
                .map(m -> {
                    AndroidMapDTO dto = new AndroidMapDTO();
                    dto.setId(m.getPermalink());
                    dto.setTitle(m.getName());
                    dto.setDistance(m.getLength());
                    dto.setTags(m.getTags());
                    dto.setNegativeElevation(Math.round(m.getNegativeElevation()));
                    dto.setPositiveElevation(Math.round(m.getPositiveElevation()));
                    dto.setTime(m.getPostedAt().atStartOfDay(team.getZoneId()).toEpochSecond());
                    dto.setType(m.getType().getLabel().toLowerCase());
                    return dto;
                }).collect(Collectors.toList());

    }

    @ResponseBody
    @RequestMapping(value = "/garmin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public java.util.Map<String, List<GarminMapDTO>> mapsGarmin(@PathVariable("teamId") String teamId) {

        final Team team = teamService.get(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team : " + teamId));

        return java.util.Map.of("tracks", mapService.listMaps(teamId, 50).stream()
                .map(m -> {
                    GarminMapDTO dto = new GarminMapDTO();
                    dto.setTitle(m.getName());
                    dto.setUrl(urlService.getMapFitUrl(team, m));
                    return dto;
                }).collect(Collectors.toList()));

    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public ResponseEntity<byte[]> getMapGpxFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> gpxFile = mapService.getGpxFile(teamId, mapId);
        if (gpxFile.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/gpx+xml");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(mapId + ".gpx")
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(gpxFile.get()),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading gpx : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find gpx : " + mapId);
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/fit", method = RequestMethod.GET, produces = "application/fit")
    public ResponseEntity<byte[]> getFitFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> fitFile = mapService.getFitFile(teamId, mapId);
        if (fitFile.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/vnd.ant.fit");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(mapId + ".fit")
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(fitFile.get()),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading fit : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find fit : " + mapId);
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/image", method = RequestMethod.GET, produces = "image/png")
    public ResponseEntity<byte[]> getMapImage(@PathVariable("teamId") String teamId,
                                              @PathVariable("mapId") String mapId,
                                              @RequestParam(name = "width", defaultValue = "-1", required = false) int targetWidth) {

        final Optional<Path> imageFile = mapService.getImageFile(teamId, mapId);
        if (imageFile.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "image/png");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(mapId + ".png")
                        .build());

                byte[] bytes = Files.readAllBytes(imageFile.get());
                if (targetWidth != -1) {
                    bytes = thumbnailService.resizeImage(bytes, targetWidth, FileExtension.PNG);
                }

                return new ResponseEntity<>(
                        bytes,
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading image : " + mapId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find image : " + mapId);
    }

    public String getMapOGDescription(Map map) {

        StringBuilder sb = new StringBuilder();
        sb.append("Distance ").append(map.getLength()).append("km").append(" - ");
        sb.append(map.getPositiveElevation()).append("m D+").append(" - ");
        sb.append(map.getType().getLabel());
        if (!map.getTags().isEmpty()) {
            sb.append(" - ");
            map.getTags().forEach(t -> sb.append(t).append(", "));
        }

        return sb.toString();

    }

}
