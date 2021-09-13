package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.team.WebPage;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}")
public class TeamController extends AbstractController {

    @GetMapping
    public String getFeed(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        final TeamConfiguration teamConfiguration = team.getConfiguration();
        if (teamConfiguration.getDefaultPage().equals(WebPage.MAPS)) {
            return redirectToMaps(team.getId());
        }
        if (teamConfiguration.getDefaultPage().equals(WebPage.RIDES)) {
            return redirectToRides(team.getId());
        }

        addGlobalValues(principal, model, team.getName(), team);
        model.addAttribute("feed", teamService.listFeed(team.getId()));
        return "team_root";
    }

    @GetMapping(value = "/join/{userId}")
    public String joinTeam(@PathVariable("teamId") String teamId,
                           @PathVariable("userId") String userId,
                           Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<User> optionalUser = userService.get(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalUser.isPresent()) {

            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {
                team.addRole(user, Role.MEMBER);
                teamService.save(team);
            }

        }

        return redirectToFeed(team.getId());
    }

    @GetMapping(value = "/leave/{userId}")
    public String leaveTeam(@PathVariable("teamId") String teamId,
                            @PathVariable("userId") String userId,
                            Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<User> optionalUser = userService.get(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalUser.isPresent()) {

            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {
                team.removeRole(userId);
                user.removeRole(team.getId()); // needed for hibernate
                teamService.save(team);
            }

        }

        return redirectToFeed(team.getId());
    }

}
