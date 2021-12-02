package info.tomacla.biketeam.web.team.user;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
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
                           Principal principal, Model model) {
        final Team team = checkTeam(teamId);
        addGlobalValues(principal, model, "Administration - Utilisateurs", team);
        model.addAttribute("roles", team.getRoles());
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_users";
    }

    @PostMapping
    public RedirectView addUser(@PathVariable("teamId") String teamId,
                                @RequestParam("stravaId") Long stravaId,
                                RedirectAttributes attributes,
                                Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            final Optional<User> optionalUser = userService.getByStravaId(stravaId);
            User target;
            if (optionalUser.isEmpty()) {
                target = new User();
                target.setStravaId(stravaId);
            } else {
                target = optionalUser.get();
            }

            userService.save(target);

            if (!team.isMember(target)) {
                userRoleService.save(new UserRole(team, target, Role.MEMBER));
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

    @GetMapping(value = "/promote/{userId}")
    public RedirectView promoteUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                                    RedirectAttributes attributes,
                                    Principal principal,
                                    Model model) {

        final Team team = checkTeam(teamId);

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            final Optional<UserRole> existingUserRole = userRoleService.get(team, user);
            if (existingUserRole.isPresent()) {
                final UserRole userRole = existingUserRole.get();
                userRole.setRole(Role.ADMIN);
                userRoleService.save(userRole);
            } else {
                userRoleService.save(new UserRole(team, user, Role.ADMIN));
            }

            return viewHandler.redirectView(team, "/admin/users");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/users");
        }


    }

    @GetMapping(value = "/relegate/{userId}")
    public RedirectView relegateUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                                     RedirectAttributes attributes,
                                     Principal principal,
                                     Model model) {

        final Team team = checkTeam(teamId);

        try {
            final User user = userService.get(userId).orElseThrow(() -> new IllegalArgumentException("User unknown"));
            final Optional<UserRole> existingUserRole = userRoleService.get(team, user);
            if (existingUserRole.isPresent()) {
                final UserRole userRole = existingUserRole.get();
                userRole.setRole(Role.MEMBER);
                userRoleService.save(userRole);
            } else {
                userRoleService.save(new UserRole(team, user, Role.MEMBER));
            }

            return viewHandler.redirectView(team, "/admin/users");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/users");
        }

    }


}
