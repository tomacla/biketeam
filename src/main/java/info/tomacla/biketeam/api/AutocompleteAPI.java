package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.map.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.domain.map.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/autocomplete")
public class AutocompleteAPI {

    @Autowired
    private MapRepository mapRepository;

    @ResponseBody
    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public List<String> getTags(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) {
            return mapRepository.findAllDistinctTags();
        }
        return mapRepository.findDistinctTagsContainer(q.toLowerCase());
    }

    @ResponseBody
    @RequestMapping(value = "/maps", method = RequestMethod.GET)
    public Map<String, String> listMaps(@RequestParam("q") String q) {
        List<MapIdNamePostedAtVisibleProjection> maps = q == null || q.isBlank() ? mapRepository.findAllByOrderByPostedAtDesc()
                : mapRepository.findAllByNameContainingIgnoreCaseOrderByPostedAtDesc(q);
        return maps.stream().collect(Collectors.toMap(MapIdNamePostedAtVisibleProjection::getId, MapIdNamePostedAtVisibleProjection::getName));

    }

}
