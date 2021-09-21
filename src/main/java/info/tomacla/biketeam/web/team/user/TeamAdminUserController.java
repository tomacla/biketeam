package info.tomacla.biketeam.web.team.user;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/{teamId}/admin/users")
public class TeamAdminUserController extends AbstractController {

    @GetMapping
    public String getUsers(@PathVariable("teamId") String teamId, Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        addGlobalValues(principal, model, "Administration - Utilisateurs", team);
        model.addAttribute("users", userService.listUsers(team));
        return "team_admin_users";
    }

    @PostMapping
    public String addUser(@PathVariable("teamId") String teamId, Principal principal, Model model,
                          @RequestParam("stravaId") Long stravaId) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        User user = userService.getByStravaId(stravaId).orElse(new User(false, "Inconnu", "Inconnu", stravaId,
                null, null, null, null, null));

        if (!user.hasTeam(team)) {
            user.addRole(team, Role.MEMBER);
        }

        userService.save(user);

        return createRedirect(team, "/admin/users");

    }

    @GetMapping(value = "/promote/{userId}")
    public String promoteUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                              Principal principal,
                              Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            user.removeRole(team.getId());
            user.addRole(team, Role.ADMIN);
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return createRedirect(team, "/admin/users");

    }

    @GetMapping(value = "/relegate/{userId}")
    public String relegateUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                               Principal principal,
                               Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            user.removeRole(team.getId());
            user.addRole(team, Role.MEMBER);
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return createRedirect(team, "/admin/users");
    }


}
