package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.service.MapService;
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
@RequestMapping(value = "/maps")
public class MapController extends AbstractController {

    @Autowired
    private MapService mapService;

    @GetMapping(value = "/{mapId}")
    public String getMap(@PathVariable("mapId") String mapId,
                         Principal principal,
                         Model model) {

        Optional<Map> optionalMap = mapService.get(mapId);
        if (optionalMap.isEmpty()) {
            return "redirect:/maps";
        }

        Map map = optionalMap.get();
        addGlobalValues(principal, model, "Map " + map.getName());
        model.addAttribute("map", map);
        return "map";

    }

    @GetMapping
    public String getMaps(@RequestParam(value = "lowerDistance", required = false, defaultValue = "1") double lowerDistance,
                          @RequestParam(value = "upperDistance", required = false, defaultValue = "1000") double upperDistance,
                          @RequestParam(value = "sort", required = false) MapSorterOption sort,
                          @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                          @RequestParam(value = "tags", required = false) List<String> tags,
                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "pageSize", defaultValue = "9", required = false) int pageSize,
                          Principal principal,
                          Model model) {

        SearchMapForm form = SearchMapForm.builder()
                .withTags(tags == null ? configurationService.getDefaultSearchTags() : tags)
                .withSort(sort)
                .withWindDirection(windDirection)
                .withLowerDistance(lowerDistance)
                .withUpperDistance(upperDistance)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        SearchMapForm.SearchMapFormParser parser = form.parser();

        Page<Map> maps = mapService.searchMaps(form.getPage(), form.getPageSize(), parser.getSort(),
                parser.getLowerDistance(),
                parser.getUpperDistance(),
                parser.getTags(),
                parser.getWindDirection());

        addGlobalValues(principal, model, "Maps");
        model.addAttribute("maps", maps.getContent());
        model.addAttribute("pages", maps.getTotalPages());
        model.addAttribute("tags", mapService.listTags());
        model.addAttribute("formdata", form);
        return "maps";
    }


}