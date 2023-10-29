package info.tomacla.biketeam.web.team.user;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}/admin/admins")
public class TeamAdminAdminController extends AbstractController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public String getAdmins(@PathVariable("teamId") String teamId,
                            @ModelAttribute("error") String error,
                            Principal principal, Model model) {
        final Team team = checkTeam(teamId);

        if (team.getVisibility().equals(Visibility.USER)) {
            return viewHandler.redirect(team, "/admin/users");
        }

        addGlobalValues(principal, model, "Administration - Administrateurs", team);
        model.addAttribute("roles", team.getRoles()
                .stream()
                .filter(r -> r.getRole().equals(Role.ADMIN))
                .sorted(Comparator.comparing(r -> r.getUser().getIdentity())).collect(Collectors.toList()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_admins";
    }

    @PostMapping
    public RedirectView addAdmin(Principal principal, Model model,
                                 RedirectAttributes attributes,
                                 @PathVariable("teamId") String teamId,
                                 @RequestParam(value = "stravaId", required = false) Long stravaId,
                                 @RequestParam(value = "email", required = false) String email) {

        final Team team = checkTeam(teamId);

        if (team.getVisibility().equals(Visibility.USER)) {
            return viewHandler.redirectView(team, "/admin/users");
        }

        try {

            User user = null;
            if (stravaId != null) {
                user = userService.getByStravaId(stravaId).orElseGet(() -> {
                    User u = new User();
                    u.setStravaId(stravaId);
                    return u;
                });
            } else if (email != null) {
                user = userService.getByEmail(email.toLowerCase()).orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    return u;
                });
            }

            if (user != null) {
                userService.save(user);

                final Optional<UserRole> existingUserRole = userRoleService.get(team, user);
                if (existingUserRole.isPresent()) {
                    final UserRole userRole = existingUserRole.get();
                    userRole.setRole(Role.ADMIN);
                    userRoleService.save(userRole);
                } else {
                    userRoleService.save(new UserRole(team, user, Role.ADMIN));
                }
            }

            return viewHandler.redirectView(team, "/admin/admins");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/admins");
        }

    }

    @GetMapping(value = "/promote/{userId}")
    public RedirectView promoteUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                                    RedirectAttributes attributes,
                                    Principal principal,
                                    Model model) {

        final Team team = checkTeam(teamId);

        if (team.getVisibility().equals(Visibility.USER)) {
            return viewHandler.redirectView(team, "/admin/users");
        }

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

            return viewHandler.redirectView(team, "/admin/admins");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/admins");
        }


    }

    @GetMapping(value = "/relegate/{userId}")
    public RedirectView relegateUser(@PathVariable("teamId") String teamId, @PathVariable("userId") String userId,
                                     RedirectAttributes attributes,
                                     Principal principal,
                                     Model model) {

        final Team team = checkTeam(teamId);

        if (team.getVisibility().equals(Visibility.USER)) {
            return viewHandler.redirectView(team, "/admin/users");
        }

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

            return viewHandler.redirectView(team, "/admin/admins");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/admins");
        }

    }


}
