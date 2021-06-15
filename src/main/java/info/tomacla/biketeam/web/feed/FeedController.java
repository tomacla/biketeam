package info.tomacla.biketeam.web.feed;

import info.tomacla.biketeam.domain.team.Page;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.service.FeedService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping(value = "/{teamId}")
public class FeedController extends AbstractController {

    @Autowired
    private FeedService feedService;

    @GetMapping
    public String getFeed(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        final TeamConfiguration teamConfiguration = team.getConfiguration();
        if (teamConfiguration.getDefaultPage().equals(Page.MAPS)) {
            return redirectToMaps(teamId);
        }
        if (teamConfiguration.getDefaultPage().equals(Page.RIDES)) {
            return redirectToRides(teamId);
        }

        addGlobalValues(principal, model, "Accueil", team);
        model.addAttribute("feed", feedService.listFeed(teamId));
        return "team_root";
    }


}
