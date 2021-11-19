package info.tomacla.biketeam.web.admin.team;

import info.tomacla.biketeam.web.AbstractController;
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

    @GetMapping
    public String getTeams(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Groupes", null);
        model.addAttribute("teams", teamService.list());
        return "admin_teams";
    }

    @GetMapping(value = "/delete/{teamId}")
    public String deleteTeam(@PathVariable("teamId") String teamId,
                             Model model) {

        try {
            teamService.delete(teamId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/teams";
    }


}
