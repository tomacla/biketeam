package info.tomacla.biketeam.web.feed;

import info.tomacla.biketeam.domain.global.Page;
import info.tomacla.biketeam.domain.global.SiteConfiguration;
import info.tomacla.biketeam.service.FeedService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/")
public class FeedController extends AbstractController {

    @Autowired
    private FeedService feedService;

    @GetMapping
    public String getFeed(Principal principal, Model model) {

        final SiteConfiguration siteConfiguration = configurationService.getSiteConfiguration();
        if (siteConfiguration.getDefaultPage().equals(Page.MAPS)) {
            return "redirect:/maps";
        }
        if (siteConfiguration.getDefaultPage().equals(Page.RIDES)) {
            return "redirect:/rides";
        }

        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("feed", feedService.listFeed());
        return "root";
    }

    @GetMapping(value = "/login-error")
    public String loginError(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("errors", List.of("Erreur de connexion"));
        model.addAttribute("feed", feedService.listFeed());
        return "root";
    }

}
