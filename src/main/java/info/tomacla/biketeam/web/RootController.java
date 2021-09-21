package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.Country;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRole;
import info.tomacla.biketeam.service.FacebookService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.web.team.NewTeamForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/")
public class RootController extends AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    private FacebookService facebookService;

    @GetMapping
    public String getRoot(Principal principal, Model model) {

        final Optional<User> userFromPrincipal = getUserFromPrincipal(principal);
        if (userFromPrincipal.isPresent()) {
            final User user = userFromPrincipal.get();
            final Set<String> teamIds = user.getRoles().stream().map(ur -> ur.getTeam().getId()).collect(Collectors.toSet());
            final List<Feed> feeds = teamService.listFeed(teamIds);
            addGlobalValues(principal, model, "Accueil", null);
            model.addAttribute("feed", feeds);
            model.addAttribute("user", user);
            model.addAttribute("userTeams", user.getRoles().stream().map(UserRole::getTeam).collect(Collectors.toList()));
            return "root_auth";
        } else {
            addGlobalValues(principal, model, "Accueil", null);
            model.addAttribute("teams", teamService.getLast4());
            return "root";
        }

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

            final Team newTeam = new Team(parser.getId().toLowerCase(),
                    parser.getName(),
                    parser.getCity(),
                    parser.getCountry(),
                    parser.getTimezone(),
                    parser.getDescription(),
                    null);

            newTeam.addRole(targetAdmin, Role.ADMIN);

            teamService.save(newTeam);
            teamService.initTeamImage(newTeam);

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

    @GetMapping(value = "/login-error")
    public String loginError(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil", null);
        model.addAttribute("errors", List.of("Erreur de connexion"));
        model.addAttribute("teams", teamService.getLast4());
        return "root";
    }

    @GetMapping(value = "/integration/facebook/login")
    public String getSiteIntegration(@RequestParam("state") String teamId,
                                     @RequestParam("code") String facebookCode,
                                     Principal principal,
                                     Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        final String userAccessToken = facebookService.getUserAccessToken(facebookCode);
        team.getIntegration().setFacebookAccessToken(userAccessToken);

        teamService.save(team);

        return createRedirect(team, "/admin/integration");

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
