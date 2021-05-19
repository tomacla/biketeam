package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private MapRepository mapRepository;

    @GetMapping(value = "/{mapId}")
    public String getMap(@PathVariable("mapId") String mapId,
                         Principal principal,
                         Model model) {

        Optional<Map> optionalMap = mapRepository.findById(mapId);
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
                          @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
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

        Page<Map> maps = getMapsFromRepository(form);

        addGlobalValues(principal, model, "Maps");
        model.addAttribute("maps", maps.getContent());
        model.addAttribute("pages", maps.getTotalPages());
        model.addAttribute("tags", mapRepository.findAllDistinctTags());
        model.addAttribute("formdata", form);
        return "maps";
    }

    private Page<Map> getMapsFromRepository(SearchMapForm form) {
        SearchMapForm.SearchMapFormParser parser = form.parser();
        Sort sort = Sort.by("postedAt").descending();
        if (parser.getSort() != null) {
            if (parser.getSort().equals(MapSorterOption.SHORT)) {
                sort = Sort.by("length").ascending();
            } else if (parser.getSort().equals(MapSorterOption.LONG)) {
                sort = Sort.by("length").descending();
            } else if (parser.getSort().equals(MapSorterOption.HILLY)) {
                sort = Sort.by("positiveElevation").descending();
            } else if (parser.getSort().equals(MapSorterOption.FLAT)) {
                sort = Sort.by("positiveElevation").ascending();
            }
        }
        Pageable pageable = PageRequest.of(form.getPage(), form.getPageSize(), sort);

        SearchMapSpecification spec = new SearchMapSpecification(form);

        return mapRepository.findAll(spec, pageable);

    }


}
