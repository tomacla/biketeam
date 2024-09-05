package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.garmin.GarminAuthService;
import info.tomacla.biketeam.service.garmin.GarminCourseService;
import info.tomacla.biketeam.service.garmin.GarminMapDescriptor;
import info.tomacla.biketeam.service.garmin.GarminToken;
import info.tomacla.biketeam.service.gpx.MapData;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}/maps")
public class MapController extends AbstractController {

    @Autowired
    private RideService rideService;

    @Autowired
    private MapService mapService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private GarminAuthService garminAuthService;

    @Autowired
    private GarminCourseService garminCourseService;

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
        model.addAttribute("_embed", embed);
        model.addAttribute("_fullSize", true);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "map";

    }

    @GetMapping
    public String getMaps(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "lowerDistance", required = false) Double lowerDistance,
                          @RequestParam(value = "upperDistance", required = false) Double upperDistance,
                          @RequestParam(value = "lowerPositiveElevation", required = false) Double lowerPositiveElevation,
                          @RequestParam(value = "upperPositiveElevation", required = false) Double upperPositiveElevation,
                          @RequestParam(value = "sort", required = false) MapSorterOption sort,
                          @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                          @RequestParam(value = "type", required = false) MapType type,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "tags", required = false) List<String> tags,
                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "pageSize", defaultValue = "18", required = false) int pageSize,
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
                .withName(name)
                .get();

        SearchMapForm.SearchMapFormParser parser = form.parser();

        Page<Map> maps = mapService.searchMaps(
                Set.of(team.getId()),
                parser.getName(),
                parser.getLowerDistance(),
                parser.getUpperDistance(),
                parser.getType(),
                parser.getLowerPositiveElevation(),
                parser.getUpperPositiveElevation(),
                parser.getTags(),
                parser.getWindDirection(),
                null, null,
                parser.getPage(),
                parser.getPageSize(),
                parser.getSort());

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
                                                          @RequestParam("q") String q,
                                                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                          @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize) {
        return mapService.listMaps(teamId, q, page, pageSize)
                .stream()
                .collect(Collectors.toMap(Map::getId, Map::getName));

    }

    @ResponseBody
    @RequestMapping(value = "/android", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AndroidMapDTO> mapsAndroid(@PathVariable("teamId") String teamId) {

        Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team"));

        return mapService.listMaps(teamId, null, 0, 50).stream()
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

        List<RideGroup> rideGroups = rideService.listRideGroupsByStartProximity(teamId);
        if (!rideGroups.isEmpty()) {
            return java.util.Map.of("tracks", rideGroups.stream()
                    .map(rideGroup -> {
                        GarminMapDTO dto = new GarminMapDTO();
                        dto.setTitle(rideService.getShortName(rideGroup.getRide()) + " " + rideGroup.getName());
                        dto.setUrl(urlService.getMapFitUrl(team, rideGroup.getMap()));
                        return dto;
                    }).collect(Collectors.toList()));
        } else {
            return java.util.Map.of("tracks", mapService.listMaps(teamId, null, 0, 50).stream()
                    .map(map -> {
                        GarminMapDTO dto = new GarminMapDTO();
                        dto.setTitle(map.getName());
                        dto.setUrl(urlService.getMapFitUrl(team, map));
                        return dto;
                    }).collect(Collectors.toList()));
        }
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public ResponseEntity<byte[]> getMapGpxFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> gpxFile = mapService.getGpxFile(teamId, mapId);
        final Optional<Map> map = mapService.get(teamId, mapId);
        if (map.isPresent() && gpxFile.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/gpx+xml");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(Optional.ofNullable(map.get().getPermalink()).orElse(map.get().getId()) + ".gpx")
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

    @RequestMapping(value = "/{mapId}/garmin", method = RequestMethod.GET)
    public void uploadMapGarmin(HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session,
                                @PathVariable("teamId") String teamId,
                                @PathVariable("mapId") String mapId) throws Exception {

        GarminToken token = garminAuthService.queryToken(request, response, session);
        if (token != null) {
            final Optional<Path> gpxFile = mapService.getGpxFile(teamId, mapId);
            final Optional<Map> optionalMap = mapService.get(teamId, mapId);
            if (gpxFile.isPresent() && optionalMap.isPresent()) {

                Map map = optionalMap.get();

                GarminMapDescriptor descriptor = new GarminMapDescriptor(
                        gpxFile.get(),
                        Optional.ofNullable(map.getPermalink()).orElse(map.getId()),
                        map.getType(),
                        map.getLength(),
                        map.getPositiveElevation(),
                        map.getNegativeElevation()
                );

                String url = garminCourseService.upload(request, response, session, token, descriptor);
                if (url != null) {
                    response.sendRedirect(url);
                }
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/{mapId}/fit", method = RequestMethod.GET, produces = "application/fit")
    public ResponseEntity<byte[]> getFitFile(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        final Optional<Path> fitFile = mapService.getFitFile(teamId, mapId);
        final Optional<Map> map = mapService.get(teamId, mapId);
        if (map.isPresent() && fitFile.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/vnd.ant.fit");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(Optional.ofNullable(map.get().getPermalink()).orElse(map.get().getId()) + ".fit")
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
    @RequestMapping(value = "/{mapId}/data", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<MapData> getMapData(@PathVariable("teamId") String teamId, @PathVariable("mapId") String mapId) {
        Optional<MapData> mapData = mapService.getMapData(teamId, mapId);
        if (mapData.isPresent()) {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("map-data.json")
                    .build());

            return new ResponseEntity<>(
                    mapData.get(),
                    headers,
                    HttpStatus.OK
            );

        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find data : " + mapId);
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

    @GetMapping(value = "/{mapId}/add-favorite")
    public RedirectView addMapFavorite(@PathVariable("teamId") String teamId,
                                       @PathVariable("mapId") String mapId,
                                       RedirectAttributes attributes,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
            if (optionalMap.isEmpty()) {
                return viewHandler.redirectView(team, "/");
            }

            Map targetMap = optionalMap.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {

                User connectedUser = optionalConnectedUser.get();

                connectedUser.getMapFavorites().add(targetMap);
                userService.save(connectedUser);

            }

            return viewHandler.redirectView(team, "/maps/" + mapId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/maps/" + mapId);
        }
    }

    @GetMapping(value = "/{mapId}/remove-favorite")
    public RedirectView removeFavorite(@PathVariable("teamId") String teamId,
                                       @PathVariable("mapId") String mapId,
                                       RedirectAttributes attributes,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
            if (optionalMap.isEmpty()) {
                return viewHandler.redirectView(team, "/");
            }

            Map targetMap = optionalMap.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {

                User connectedUser = optionalConnectedUser.get();

                connectedUser.getMapFavorites().removeIf(map -> map.getId().equals(targetMap.getId()));
                userService.save(connectedUser);

            }

            return viewHandler.redirectView(team, "/maps/" + mapId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/maps/" + mapId);
        }
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
