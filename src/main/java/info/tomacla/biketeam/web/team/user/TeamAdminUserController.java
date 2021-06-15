package info.tomacla.biketeam.web.team.user;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRole;
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

        checkAdmin(principal, teamId);

        final Team team = checkTeam(teamId);
        addGlobalValues(principal, model, "Administration - Utilisateurs", team);
        model.addAttribute("users", userService.listUsers(teamId));
        return "team_admin_users";
    }

    @PostMapping
    public String addUser(@PathVariable("teamId") String teamId, Principal principal, Model model,
                          @RequestParam("stravaId") Long stravaId) {

        checkAdmin(principal, teamId);

        User user = new User(false, "Inconnu", "Inconnu", stravaId,
                null, null, null, null, null);

        user.addRole(UserRole.member(user.getId(), teamId));

        userService.save(user);

        return "redirect:/" + teamId + "/admin/users";

    }

    @GetMapping(value = "/promote/{userId}")
    public String promoteUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                              Principal principal,
                              Model model) {

        checkAdmin(principal, teamId);

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            user.removeRole(UserRole.member(user.getId(), teamId));
            user.addRole(UserRole.admin(user.getId(), teamId));
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/" + teamId + "/admin/users";
    }

    @GetMapping(value = "/relegate/{userId}")
    public String relegateUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                               Principal principal,
                               Model model) {

        checkAdmin(principal, teamId);

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            user.removeRole(UserRole.admin(user.getId(), teamId));
            user.addRole(UserRole.member(user.getId(), teamId));
            userService.save(user);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/" + teamId + "/admin/users";
    }


}
