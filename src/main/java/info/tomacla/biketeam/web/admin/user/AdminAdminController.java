package info.tomacla.biketeam.web.admin.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/admin/admins")
public class AdminAdminController extends AbstractController {

    @GetMapping
    public String getAdmins(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Administrateurs", null);
        model.addAttribute("admins", userService.listAdmins());
        return "admin_admins";
    }

    @PostMapping
    public String addAdmin(Principal principal, Model model,
                           @RequestParam(value = "stravaId", required = false) Long stravaId,
                           @RequestParam(value = "email", required = false) String email) {

        User target = null;
        if (stravaId != null) {
            target = userService.getByStravaId(stravaId).orElseGet(() -> {
                User u = new User();
                u.setStravaId(stravaId);
                return u;
            });
        } else if (email != null) {
            target = userService.getByEmail(email.toLowerCase()).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                return u;
            });
        }

        target.setAdmin(true);

        userService.save(target);

        return "redirect:/admin/admins";

    }

    @GetMapping(value = "/promote/{userId}")
    public String promoteUser(@PathVariable("userId") String userId,
                              Model model) {

        try {
            userService.promote(userId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/admins";
    }

    @GetMapping(value = "/relegate/{userId}")
    public String relegateUser(@PathVariable("userId") String userId,
                               Principal principal,
                               Model model) {

        try {
            User connectedUser = getUserFromPrincipal(principal).get();
            if (!connectedUser.getId().equals(userId)) {
                userService.relegate(userId);
            }
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/admins";
    }

}
