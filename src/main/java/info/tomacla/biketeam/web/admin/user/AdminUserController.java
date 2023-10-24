package info.tomacla.biketeam.web.admin.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/admin/users")
public class AdminUserController extends AbstractController {

    @GetMapping
    public String getUsers(@RequestParam(value = "name", defaultValue = "", required = false) String name,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
                           Principal principal,
                           Model model) {

        addGlobalValues(principal, model, "Administration - Utilisateurs", null);
        Page<User> users = userService.listUsers(name, page, pageSize);
        model.addAttribute("users", users.getContent());
        model.addAttribute("matches", users.getTotalElements());
        model.addAttribute("pages", users.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);
        return "admin_users";
    }

    @PostMapping(value = "/merge")
    public String mergeUsers(Principal principal, Model model,
                             @RequestParam("sourceId") String sourceId,
                             @RequestParam("targetId") String targetId) {

        try {

            User connectedUser = getUserFromPrincipal(principal).get();
            if (!connectedUser.getId().equals(sourceId)) {
                userService.merge(sourceId, targetId);
            }

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";

    }


    @GetMapping(value = "/delete/{userId}")
    public String deleteUser(@PathVariable("userId") String userId, Principal principal, Model model) {

        try {

            User connectedUser = getUserFromPrincipal(principal).get();
            if (!connectedUser.getId().equals(userId)) {
                userService.get(userId).ifPresent(user -> userService.delete(user.getId()));
            }

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }


}
