package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.parameter.ParameterRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.feed.FeedService;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.web.map.SearchMapForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/catalog")
public class CatalogController extends AbstractController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private MapService mapService;

    @Autowired
    private TripService tripService;

    @GetMapping(value = {"", "/"})
    public String getCatalog(@ModelAttribute("error") String error,
                             Principal principal,
                             Model model) {

        return "redirect:/catalog/maps";

    }

    @GetMapping("/maps")
    public String getMaps(@RequestParam(value = "lowerDistance", required = false) Double lowerDistance,
                          @RequestParam(value = "upperDistance", required = false) Double upperDistance,
                          @RequestParam(value = "lowerPositiveElevation", required = false) Double lowerPositiveElevation,
                          @RequestParam(value = "upperPositiveElevation", required = false) Double upperPositiveElevation,
                          @RequestParam(value = "sort", required = false) MapSorterOption sort,
                          @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                          @RequestParam(value = "type", required = false) MapType type,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "centerAddress", required = false) String centerAddress,
                          @RequestParam(value = "centerAddressLat", required = false) Double centerAddressLat,
                          @RequestParam(value = "centerAddressLng", required = false) Double centerAddressLng,
                          @RequestParam(value = "distanceToCenter", required = false) Integer distanceToCenter,
                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "pageSize", defaultValue = "18", required = false) int pageSize,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        Page<Team> publicTeams = teamService.searchTeams(0, 100000, null, List.of(Visibility.PUBLIC));
        Set<String> teamIds = publicTeams.stream().map(Team::getId).collect(Collectors.toSet());

        // if connected, add followed teams
        getUserFromPrincipal(principal).ifPresent(user -> teamService.getUserTeams(user).forEach(t -> teamIds.add(t.getTeamId())));

        SearchMapForm.SearchMapFormBuilder formBuilder = SearchMapForm.builder()
                .withSort(sort)
                .withWindDirection(windDirection)
                .withLowerDistance(lowerDistance)
                .withUpperDistance(upperDistance)
                .withLowerPositiveElevation(lowerPositiveElevation)
                .withUpperPositiveElevation(upperPositiveElevation)
                .withPage(page)
                .withPageSize(pageSize)
                .withType(type)
                .withName(name);

        if (centerAddress != null && !centerAddress.isBlank() && centerAddressLat != null && centerAddressLng != null && distanceToCenter != null) {
            formBuilder.withCenterAddress(centerAddress)
                    .withCenterAddressPoint(new Point(centerAddressLat, centerAddressLng))
                    .withDistanceToCenter(distanceToCenter);
        }

        SearchMapForm form = formBuilder.get();

        SearchMapForm.SearchMapFormParser parser = form.parser();

        Page<Map> maps = mapService.searchMaps(
                teamIds,
                parser.getName(),
                parser.getLowerDistance(),
                parser.getUpperDistance(),
                parser.getType(),
                parser.getLowerPositiveElevation(),
                parser.getUpperPositiveElevation(),
                parser.getTags(),
                parser.getWindDirection(),
                parser.getCenterAddressPoint(),
                parser.getDistanceToCenter(),
                parser.getPage(),
                parser.getPageSize(),
                parser.getSort());

        addGlobalValues(principal, model, "Maps", null);
        model.addAttribute("maps", maps.getContent());
        model.addAttribute("pages", maps.getTotalPages());
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "catalog_maps";

    }

    @GetMapping("/trips")
    public String getTrips(@ModelAttribute("error") String error,
                           Principal principal,
                           Model model) {

        Page<Trip> trips = tripService.searchTrips(
                null,
                null,
                null,
                null,
                true,
                0,
                10000
        );

        addGlobalValues(principal, model, "Trips", null);
        model.addAttribute("trips", trips.getContent());
        model.addAttribute("pages", trips.getTotalPages());
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "catalog_trips";

    }

    @GetMapping(value = "/trips/{tripId}")
    public String getTrip(@PathVariable("tripId") String tripId,
                          @ModelAttribute("error") String error,
                          Authentication authentication,
                          Principal principal,
                          Model model) {


        Optional<Trip> optionalTrip = tripService.findById(tripId);
        if (optionalTrip.isEmpty()) {
            return "redirect:/catalog/trips";
        }
        Trip trip = optionalTrip.get();
        final Team team = checkTeam(trip.getTeamId());

        if (!trip.isPublishToCatalog() || (!trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team))) {
            return "redirect:/catalog/trips";
        }

        boolean canAccess = team.isPublic();
        if (!canAccess && authentication != null) {
            canAccess = userService.authorizePublicAccess(authentication, team.getId());
            if (!canAccess && getUserFromPrincipal(principal).isPresent()) {
                canAccess = userService.authorizeAuthenticatedPublicAccess(authentication, team.getId()) || userService.authorizeAdminAccess(authentication, team.getId());
            }
        }

        if (!canAccess) {
            return "redirect:/catalog/trips";
        }

        addGlobalValues(principal, model, "Trip " + trip.getTitle(), null);
        model.addAttribute("trip", trip);
        model.addAttribute("teamId", team.getId());
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "catalog_trip";
    }

    @GetMapping("/favorites")
    public String getFavorites(@ModelAttribute("error") String error,
                               Principal principal,
                               Model model) {

        final Optional<User> userFromPrincipal = getUserFromPrincipal(principal);
        if (userFromPrincipal.isPresent()) {

            addGlobalValues(principal, model, "Favoris", null);
            if (!ObjectUtils.isEmpty(error)) {
                model.addAttribute("errors", List.of(error));
            }
            return "catalog_favorites";

        }

        return "redirect:/catalog/maps";

    }

}
