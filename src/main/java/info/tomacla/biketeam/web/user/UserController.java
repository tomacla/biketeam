package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractController {

    @GetMapping(value = "/me")
    public String getUser(Principal principal,
                          Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        addGlobalValues(principal, model, "Mon profil");
        model.addAttribute("user", user);
        return "user";


    }

    @PostMapping(value = "/me")
    public String updateUser(Principal principal,
                             Model model,
                             @RequestParam("email") String email) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();
        user.setEmail(email);

        userService.save(user);

        addGlobalValues(principal, model, "Mon profil");
        model.addAttribute("user", user);
        return "user";


    }

}
