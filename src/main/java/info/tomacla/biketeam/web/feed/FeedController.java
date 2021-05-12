package info.tomacla.biketeam.web.feed;

import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.List;

@Controller
@RequestMapping(value = "/")
public class FeedController extends AbstractController {

    @Autowired
    private FeedRepository feedRepository;

    @GetMapping
    public String getFeed(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("feed", getFeedFromRepository().getContent());
        return "root";
    }

    @GetMapping(value = "/login-error")
    public String loginError(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil");
        model.addAttribute("errors", List.of("Erreur de connexion"));
        model.addAttribute("feed", getFeedFromRepository().getContent());
        return "root";
    }

    private Page<Feed> getFeedFromRepository() {
        return feedRepository.findAllByPublishedAtLessThan(
                ZonedDateTime.now(configurationService.getTimezone()),
                PageRequest.of(0, 15, Sort.by("publishedAt").descending()));
    }

}
