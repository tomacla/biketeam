package info.tomacla.biketeam.web.admin.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/admin/users")
public class AdminUserController extends AbstractController {

    @GetMapping
    public String getUsers(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Utilisateurs", null);
        model.addAttribute("users", userService.listUsers());
        return "admin_users";
    }

    @PostMapping
    public String addUser(Principal principal, Model model,
                          @RequestParam(value = "stravaId", required = false) Long stravaId,
                          @RequestParam(value = "email", required = false) String email) {

        User target = null;
        if(stravaId != null) {
            target = userService.getByStravaId(stravaId).orElseGet(() -> {
                User u = new User();
                u.setStravaId(stravaId);
                return u;
            });
        } else if(email != null) {
            target = userService.getByEmail(email.toLowerCase()).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                return u;
            });
        }

        userService.save(target);

        return "redirect:/admin/users";

    }

    @PostMapping(value = "/merge")
    public String mergeUsers(Principal principal, Model model,
                             @RequestParam("sourceId") String sourceId,
                             @RequestParam("targetId") String targetId) {

        userService.merge(sourceId, targetId);

        return "redirect:/admin/users";

    }

    @GetMapping(value = "/promote/{userId}")
    public String promoteUser(@PathVariable("userId") String userId,
                              Model model) {

        try {
            userService.promote(userId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }

    @GetMapping(value = "/relegate/{userId}")
    public String relegateUser(@PathVariable("userId") String userId,
                               Model model) {

        try {
            userService.relegate(userId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }

    @GetMapping(value = "/delete/{userId}")
    public String deleteUser(@PathVariable("userId") String userId, Model model) {

        try {

            userService.get(userId).ifPresent(user -> userService.delete(user));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }


}
