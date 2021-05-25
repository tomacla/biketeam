package info.tomacla.biketeam.web.admin.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/admin/users")
public class AdminUserController extends AbstractController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getUsers(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Utilisateurs");
        model.addAttribute("users", userRepository.findAll());
        return "admin_users";
    }

    @PostMapping
    public String addUser(Principal principal, Model model,
                          @RequestParam("stravaId") Long stravaId) {

        User user = new User(false, "Inconnu", "Inconnu", stravaId,
                null, null, null, null);

        userRepository.save(user);

        return "redirect:/admin/users";

    }

    @GetMapping(value = "/promote/{userId}")
    public String promoteUser(@PathVariable("userId") String userId,
                              Model model) {

        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setAdmin(true);
                userRepository.save(user);
            });

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }

    @GetMapping(value = "/relegate/{userId}")
    public String relegateUser(@PathVariable("userId") String userId,
                               Model model) {

        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setAdmin(false);
                userRepository.save(user);
            });

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }


}
