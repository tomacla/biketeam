package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.web.forms.SearchMapForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private FeedRepository feedRepository;

    @GetMapping
    public String getRoot(Principal principal, Model model) {
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
        return feedRepository.findAll(PageRequest.of(0, 10, Sort.by("publishedAt").descending()));
    }

}
