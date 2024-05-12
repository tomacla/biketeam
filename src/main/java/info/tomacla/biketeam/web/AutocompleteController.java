package info.tomacla.biketeam.web;

import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;

@Controller
@RequestMapping(value = "/autocomplete")
@PermitAll
public class AutocompleteController extends AbstractController {

    @Autowired
    private MapService mapService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @ResponseBody
    @RequestMapping(value = "/permalink/teams", method = RequestMethod.GET)
    public String autocompleteTeamPermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "20") int maxSize) {
        return teamService.getPermalink(title, maxSize, true);
    }

    @ResponseBody
    @RequestMapping(value = "/permalink/maps", method = RequestMethod.GET)
    public String autocompleteMapPermalink(@RequestParam("title") String title,
                                           @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return mapService.getPermalink(title, maxSize, false);
    }

    @ResponseBody
    @RequestMapping(value = "/permalink/rides", method = RequestMethod.GET)
    public String autocompleteRidePermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return rideService.getPermalink(title, maxSize, false);
    }

    @ResponseBody
    @RequestMapping(value = "/permalink/trips", method = RequestMethod.GET)
    public String autocompleteTripPermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return tripService.getPermalink(title, maxSize, false);
    }

}
