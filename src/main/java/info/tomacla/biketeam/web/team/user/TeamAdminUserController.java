package info.tomacla.biketeam.web.team.user;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/users")
public class TeamAdminUserController extends AbstractController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public String getUsers(@PathVariable("teamId") String teamId,
                           @ModelAttribute("error") String error,
                           @RequestParam(value = "name", defaultValue = "", required = false) String name,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
                           Principal principal, Model model) {
        final Team team = checkTeam(teamId);
        addGlobalValues(principal, model, "Administration - Utilisateurs", team);

        Page<User> users = userService.listTeamUsers(team, name, page, pageSize);
        model.addAttribute("users", users.getContent());
        model.addAttribute("matches", users.getTotalElements());
        model.addAttribute("pages", users.getTotalPages());
        model.addAttribute("page", page);
        model.addAttribute("name", name);
        model.addAttribute("pageSize", pageSize);

        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_users";
    }

    @PostMapping
    public RedirectView addUser(@PathVariable("teamId") String teamId,
                                @RequestParam(value = "stravaId", required = false) Long stravaId,
                                @RequestParam(value = "email", required = false) String email,
                                RedirectAttributes attributes,
                                Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {

            User target = null;

            if (stravaId != null) {
                final Optional<User> optionalUser = userService.getByStravaId(stravaId);

                if (optionalUser.isEmpty()) {
                    target = new User();
                    target.setStravaId(stravaId);
                } else {
                    target = optionalUser.get();
                }

            } else if (!ObjectUtils.isEmpty(email)) {
                final Optional<User> optionalUser = userService.getByEmail(email.toLowerCase());
                if (optionalUser.isEmpty()) {
                    target = new User();
                    target.setEmail(email);
                } else {
                    target = optionalUser.get();
                }
            }

            if (target != null) {
                userService.save(target);

                final Optional<UserRole> existingUserRole = userRoleService.get(team, target);
                if (existingUserRole.isEmpty()) {
                    userRoleService.save(new UserRole(team, target, Role.MEMBER));
                }

            }

            return viewHandler.redirectView(team, "/admin/users");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/users");
        }

    }

    @GetMapping(value = "/delete/{userId}")
    public RedirectView removeUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                                   Principal principal,
                                   RedirectAttributes attributes,
                                   Model model) {

        final Team team = checkTeam(teamId);

        try {

            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            userRoleService.delete(team, user);

            return viewHandler.redirectView(team, "/admin/users");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/users");
        }

    }


}
