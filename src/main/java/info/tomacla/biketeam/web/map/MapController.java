package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.UrlService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/maps")
public class MapController extends AbstractController {

    @Autowired
    private MapService mapService;

    @Autowired
    private UrlService urlService;

    @GetMapping(value = "/{mapId}")
    public String getMap(@PathVariable("teamId") String teamId,
                         @PathVariable("mapId") String mapId,
                         Principal principal,
                         Model model) {

        final Team team = checkTeam(teamId);

        Optional<Map> optionalMap = mapService.get(teamId, mapId);
        if (optionalMap.isEmpty()) {
            return redirectToMaps(teamId);
        }

        Map map = optionalMap.get();
        addOpenGraphValues(team,
                model,
                map.getName(),
                urlService.getMapImageUrl(map.getTeamId(), map.getId()),
                urlService.getMapUrl(map.getTeamId(), map.getId()),
                map.getDescription()
        );

        addGlobalValues(principal, model, "Map " + map.getName(), team);
        model.addAttribute("map", map);
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
                teamId,
                form.getPage(),
                form.getPageSize(),
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
        model.addAttribute("tags", mapService.listTags(teamId));
        model.addAttribute("formdata", form);
        return "maps";
    }

}
