package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.*;
import info.tomacla.biketeam.service.facebook.FacebookService;
import info.tomacla.biketeam.web.team.NewTeamForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/")
public class RootController extends AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    private MapService mapService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @Autowired
    private FacebookService facebookService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public String getRoot(@RequestParam(required = false, name = "error") String error,
                          @ModelAttribute(name = "error") String modelError,
                          Principal principal, Model model) {

        List<String> errors = new ArrayList<>();
        if (!ObjectUtils.isEmpty(error)) {
            errors.add(error);
        }
        if (!ObjectUtils.isEmpty(modelError)) {
            errors.add(modelError);
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
        }

        final Optional<User> userFromPrincipal = getUserFromPrincipal(principal);
        if (userFromPrincipal.isPresent()) {
            final User user = userFromPrincipal.get();

            final List<Team> teams = teamService.getUserTeams(user.getId());

            final List<Feed> feeds = teamService.listFeed(teams.stream().map(Team::getId).collect(Collectors.toSet()));
            addGlobalValues(principal, model, "Accueil", null);
            if (error != null) {
                model.addAttribute("errors", List.of(error));
            }
            model.addAttribute("feed", feeds);
            model.addAttribute("user", user);
            model.addAttribute("userTeams", teams);
            return "root_auth";
        } else {
            addGlobalValues(principal, model, "Accueil", null);
            if (error != null) {
                model.addAttribute("errors", List.of(error));
            }
            model.addAttribute("teams", teamService.getLast4());
            return "root";
        }

    }

    @GetMapping(value = "login")
    public String loginPage(@RequestParam(value = "requestUri", required = false) final String referer, Principal principal, Model model) {
        addGlobalValues(principal, model, "Connexion", null);
        model.addAttribute("referer", referer == null ? "/" : referer);
        return "login";
    }

    @GetMapping(value = "new")
    public String newTeam(Principal principal, Model model) {
        addGlobalValues(principal, model, "Créer une team", null);
        model.addAttribute("formdata", NewTeamForm.builder().get());
        model.addAttribute("timezones", getAllAvailableTimeZones());
        return "new";
    }

    @PostMapping(value = "new")
    public String submitNewTeam(NewTeamForm form, Principal principal, Model model) {

        try {

            final User targetAdmin = getUserFromPrincipal(principal).orElseThrow(() -> new IllegalStateException("User not authenticated"));

            final NewTeamForm.NewTeamFormParser parser = form.parser();

            teamService.get(parser.getId().toLowerCase()).ifPresent(team -> {
                throw new IllegalArgumentException("Team " + team.getId() + " already exists");
            });

            final Team newTeam = new Team(parser.getId().toLowerCase(),
                    parser.getName(),
                    parser.getCity(),
                    parser.getCountry(),
                    parser.getTimezone(),
                    parser.getDescription());

            teamService.save(newTeam, true);

            userRoleService.save(new UserRole(newTeam, targetAdmin, Role.ADMIN));

            return "redirect:/" + newTeam.getId();

        } catch (Exception e) {
            addGlobalValues(principal, model, "Créer une team", null);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            return "new";
        }


    }

    @GetMapping("/teams")
    public String searchTeams(@RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "country", required = false) Country country,
                              @RequestParam(value = "city", required = false) String city,
                              @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                              @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize,
                              Principal principal,
                              Model model) {

        SearchTeamForm form = SearchTeamForm.builder()
                .withName(name)
                .withCity(city)
                .withCountry(country)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchTeamForm.SearchTeamFormParser parser = form.parser();

        Page<Team> teams = teamService.searchTeams(
                parser.getPage(),
                parser.getPageSize(),
                parser.getName(),
                parser.getCity(),
                parser.getCountry()
        );

        addGlobalValues(principal, model, "Groupes", null);
        model.addAttribute("teams", teams.getContent());
        model.addAttribute("pages", teams.getTotalPages());
        model.addAttribute("formdata", form);

        return "teams";

    }

    @GetMapping(value = "/integration/facebook/login")
    public RedirectView facebookCallbackEndpoint(@RequestParam("code") String facebookCode,
                                                 Principal principal,
                                                 Model model) {

        final String userAccessToken = facebookService.getUserAccessToken(facebookCode);
        facebookService.storeToken(userAccessToken);

        // do not use super function createRedirect in this particular case
        return new RedirectView("/admin/facebook");

    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete/permalink/teams", method = RequestMethod.GET)
    public String autocompleteTeamPermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "20") int maxSize) {
        return teamService.getPermalink(title, maxSize, true);
    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete/permalink/maps", method = RequestMethod.GET)
    public String autocompleteMapPermalink(@RequestParam("title") String title,
                                           @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return mapService.getPermalink(title, maxSize, false);
    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete/permalink/rides", method = RequestMethod.GET)
    public String autocompleteRidePermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return rideService.getPermalink(title, maxSize, false);
    }

    @ResponseBody
    @RequestMapping(value = "/autocomplete/permalink/trips", method = RequestMethod.GET)
    public String autocompleteTripPermalink(@RequestParam("title") String title,
                                            @RequestParam(required = false, defaultValue = "100") int maxSize) {
        return tripService.getPermalink(title, maxSize, false);
    }

    @ResponseBody
    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
    public String robotsTxt() {
        return "User-agent: *\n" +
                "Disallow: /";
    }

    @RequestMapping(value = "/legal-mentions", method = RequestMethod.GET)
    public String termsOfService(Principal principal, Model model) {
        addGlobalValues(principal, model, "Mentions légales", null);
        return "legal_mentions";
    }

}
