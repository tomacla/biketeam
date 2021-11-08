package info.tomacla.biketeam.web.admin.facebook;

import info.tomacla.biketeam.domain.parameter.Parameter;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.externalpublication.FacebookPage;
import info.tomacla.biketeam.service.externalpublication.FacebookService;
import info.tomacla.biketeam.web.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/admin")
public class AdminFacebookController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AdminFacebookController.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private FacebookService facebookService;

    @GetMapping(value = "/facebook")
    public String facebook(Principal principal, Model model) {

        final Parameter facebookAccessToken = facebookService.getFacebookAccessToken().orElse(null);
        final List<Team> teams = teamService.list();

        final List<FacebookPage> authorizedPages = new ArrayList<>();
        try {
            authorizedPages.addAll(facebookService.getAuthorizedPages());
        } catch (Exception e) {
            log.error("Unable to fetch facebook pages", e);
        }

        addGlobalValues(principal, model, "Administration - Facebook", null);
        model.addAttribute("accessToken", facebookAccessToken == null ? null : facebookAccessToken.getValue());
        model.addAttribute("teams", teams);
        model.addAttribute("connectedAccount", facebookService.getConnectedAccount().orElse(null));
        model.addAttribute("authorizedPages", authorizedPages);
        model.addAttribute("facebookUrl", facebookService.getLoginUrl());

        return "admin_facebook";

    }

    @PostMapping(value = "/facebook")
    public String facebook(@RequestParam Map<String, String> values, Principal principal, Model model) {

        for (Map.Entry<String, String> valueEntry : values.entrySet()) {
            if (valueEntry.getKey().startsWith("facebook-")) {
                String teamId = valueEntry.getKey().replace("facebook-", "");
                teamService.get(teamId).ifPresent(team -> {
                    team.getIntegration().setFacebookPageId(valueEntry.getValue());
                    teamService.save(team);
                });
            }
        }

        return "redirect:/admin/facebook";

    }

    @GetMapping(value = "/facebook/disconnect")
    public String disconnect(Principal principal, Model model) {

        facebookService.deleteToken();

        return "redirect:/admin/facebook";

    }

}
