package info.tomacla.biketeam.web.page;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping(value = "/{teamId}/page")
public class PageController extends AbstractController {

    @GetMapping
    public String getPage(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "FAQ", team);
        model.addAttribute("faq", team.getConfiguration().getMarkdownPage());
        return "faq";

    }

}
