package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.map.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/autocomplete")
public class AutocompleteAPI {

    @Autowired
    private MapService mapService;

    @ResponseBody
    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public List<String> autocompleteTags(@RequestParam("q") String q) {
        return mapService.listTags(q);
    }

    @ResponseBody
    @RequestMapping(value = "/maps", method = RequestMethod.GET)
    public Map<String, String> autocompleteMaps(@RequestParam("q") String q) {
        return mapService.searchMaps(q)
                .stream()
                .collect(Collectors.toMap(MapIdNamePostedAtVisibleProjection::getId, MapIdNamePostedAtVisibleProjection::getName));

    }

}
