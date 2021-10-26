package info.tomacla.biketeam.web.admin.team;

import info.tomacla.biketeam.service.deletion.DeletionService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/admin/teams")
public class AdminTeamController extends AbstractController {

    @Autowired
    private DeletionService deletionService;

    @GetMapping
    public String getTeams(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Groupes", null);
        model.addAttribute("teams", teamService.list());
        return "admin_teams";
    }

    @GetMapping(value = "/delete/{teamId}")
    public String relegateTeam(@PathVariable("teamId") String teamId,
                               Model model) {

        try {
            deletionService.deleteTeam(teamId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/teams";
    }


}
