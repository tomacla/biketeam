package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        final EditUserForm form = EditUserForm.builder()
                .withEmail(user.getEmail())
                .withEmailPublishPublications(user.isEmailPublishPublications())
                .withEmailPublishRides(user.isEmailPublishRides())
                .withEmailPublishTrips(user.isEmailPublishTrips())
                .get();

        addGlobalValues(principal, model, "Mon profil", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", form);
        return "user";


    }

    @PostMapping(value = "/me")
    public String updateUser(Principal principal,
                             Model model,
                             EditUserForm form) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final EditUserForm.EditUserFormParser parser = form.parser();

        final User user = optionalConnectedUser.get();
        user.setEmail(parser.getEmail());
        user.setEmailPublishPublications(parser.isEmailPublishPublications());
        user.setEmailPublishRides(parser.isEmailPublishRides());
        user.setEmailPublishTrips(parser.isEmailPublishTrips());
        userService.save(user);

        addGlobalValues(principal, model, "Mon profil", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", form);
        return "user";


    }

}
