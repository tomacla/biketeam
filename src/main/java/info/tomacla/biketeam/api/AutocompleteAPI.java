package info.tomacla.biketeam.api;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.map.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api")
public class AutocompleteAPI {

    @Autowired
    private MapService mapService;

    @Autowired
    private TeamService teamService;

    @ResponseBody
    @RequestMapping(value = "/{teamId}/autocomplete/maps", method = RequestMethod.GET)
    public Map<String, String> autocompleteMaps(@PathVariable("teamId") String teamId,
                                                @RequestParam("q") String q) {
        return mapService.searchMaps(teamId, q)
                .stream()
                .collect(Collectors.toMap(MapIdNamePostedAtVisibleProjection::getId, MapIdNamePostedAtVisibleProjection::getName));

    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete/permatitle", method = RequestMethod.GET)
    public String autocompleteMaps(@RequestParam("title") String title) {
        String permatitle = Strings.permatitleFromString(title);
        permatitle = permatitle.toLowerCase();
        if (permatitle.length() > 20) {
            permatitle = permatitle.substring(0, 20);
        }
        for (int i = 0; teamService.idExists(permatitle); i++) {
            if (permatitle.length() == 20) {
                permatitle = permatitle.substring(0, 18);
            }
            permatitle = permatitle + (i + 2);
        }
        return permatitle;
    }

}
