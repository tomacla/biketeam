package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractController {

    @GetMapping(value = "/me")
    public String getRide(Principal principal,
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

}
