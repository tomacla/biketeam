package info.tomacla.biketeam.web;

import com.fasterxml.jackson.core.type.TypeReference;
import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.web.forms.SearchMapForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/maps")
public class MapController extends AbstractController {

    @Autowired
    private MapRepository mapRepository;

    @GetMapping
    public String getMaps(Principal principal, Model model) {
        List<String> defaultSearchTags = siteConfigurationRepository.findById(1L).get().getDefaultSearchTags();
        addGlobalValues(principal, model, "Maps");
        if (defaultSearchTags.isEmpty()) {
            model.addAttribute("maps", mapRepository.findByVisibleTrue());
        } else {
            model.addAttribute("maps", mapRepository.findByTagsInAndVisibleTrue(defaultSearchTags));
        }
        model.addAttribute("tags", mapRepository.findAllDistinctTags());
        model.addAttribute("formdata", SearchMapForm.empty(defaultSearchTags));
        return "maps";
    }

    @PostMapping
    public String getMaps(Principal principal, Model model,
                           SearchMapForm form) {
        addGlobalValues(principal, model, "Maps");

        List<Map> maps;
        @SuppressWarnings("Convert2Diamond") List<String> parsedTags = Json.parse(form.getTags(), new TypeReference<List<String>>() {
        });
        if (parsedTags.isEmpty()) {
            maps = mapRepository.findByLengthBetweenAndVisibleTrue(form.getLowerDistance(), form.getUpperDistance());
        } else {
            maps = mapRepository.findDistinctByLengthBetweenAndTagsInAndVisibleTrue(form.getLowerDistance(), form.getUpperDistance(), parsedTags);
        }

        maps.sort(MapSorter.of(form.getSort()));
        if (form.getWindDirection() != null && !form.getWindDirection().isBlank()) {
            maps.removeIf(map -> MapFilter.byWind(map, WindDirection.valueOf(form.getWindDirection())));
        }

        model.addAttribute("maps", maps);
        model.addAttribute("tags", mapRepository.findAllDistinctTags());
        model.addAttribute("formdata", form);
        return "maps";
    }

    @GetMapping(value = "/{mapId}")
    public String getMap(@PathVariable("mapId") String mapId,
                         Principal principal,
                         Model model) {
        Map map = mapRepository.findById(mapId).get();
        addGlobalValues(principal, model, "Map " + map.getName());
        model.addAttribute("map", map);
        return "map";
    }

}
