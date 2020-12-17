package info.tomacla.biketeam.web;

import info.tomacla.biketeam.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/")
public class RootController extends AbstractController {

    @Autowired
    private FeedService feedService;

    @GetMapping
    public String getRoot(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("feed", feedService.getFeed());
        return "root";
    }

    @GetMapping(value = "/login-error")
    public String loginError(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("errors", List.of("Erreur de connexion"));
        model.addAttribute("feed", feedService.getFeed());
        return "root";
    }

}
