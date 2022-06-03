package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.MapDTO;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.map.SearchMapForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/maps")
public class MapAPI extends AbstractAPI {

    @Autowired
    private MapService mapService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MapDTO>> getMaps(@PathVariable String teamId,
                                                @RequestParam(value = "lowerDistance", required = false, defaultValue = "1") double lowerDistance,
                                                @RequestParam(value = "upperDistance", required = false, defaultValue = "1000") double upperDistance,
                                                @RequestParam(value = "lowerPositiveElevation", required = false, defaultValue = "0") double lowerPositiveElevation,
                                                @RequestParam(value = "upperPositiveElevation", required = false, defaultValue = "10000") double upperPositiveElevation,
                                                @RequestParam(value = "sort", required = false) MapSorterOption sort,
                                                @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                                                @RequestParam(value = "type", required = false) MapType type,
                                                @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "tags", required = false) List<String> tags,
                                                @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                @RequestParam(value = "pageSize", defaultValue = "9", required = false) int pageSize) {

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
                team.getId(),
                parser.getPage(),
                parser.getPageSize(),
                parser.getSort(),
                parser.getName(),
                parser.getLowerDistance(),
                parser.getUpperDistance(),
                parser.getType(),
                parser.getLowerPositiveElevation(),
                parser.getUpperPositiveElevation(),
                parser.getTags(),
                parser.getWindDirection());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Pages", String.valueOf(maps.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(maps.getContent().stream().map(MapDTO::valueOf).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{mapId}", produces = "application/json")
    public ResponseEntity<MapDTO> getMap(@PathVariable String teamId, @PathVariable String mapId) {

        checkTeam(teamId);

        return mapService.get(teamId, mapId)
                .map(value -> ResponseEntity.ok().body(MapDTO.valueOf(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/tags", produces = "application/json")
    public ResponseEntity<List<String>> getTags(@PathVariable String teamId) {

        checkTeam(teamId);

        return ResponseEntity.ok().body(mapService.listTags(teamId));

    }

}
