package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.map.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/{teamId}/autocomplete")
public class AutocompleteAPI {

    @Autowired
    private MapService mapService;

    @ResponseBody
    @RequestMapping(value = "/maps", method = RequestMethod.GET)
    public Map<String, String> autocompleteMaps(@PathVariable("teamId") String teamId,
                                                @RequestParam("q") String q) {
        return mapService.searchMaps(teamId, q)
                .stream()
                .collect(Collectors.toMap(MapIdNamePostedAtVisibleProjection::getId, MapIdNamePostedAtVisibleProjection::getName));

    }

}
