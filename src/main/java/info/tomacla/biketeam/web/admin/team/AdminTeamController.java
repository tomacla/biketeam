package info.tomacla.biketeam.web.admin.team;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/admin/teams")
public class AdminTeamController extends AbstractController {

    @Autowired
    private PedalonsMigrationService pedalonsMigrationService;

    @GetMapping
    public String getTeams(@RequestParam(value = "name", defaultValue = "", required = false) String name,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize,
                           Principal principal,
                           Model model) {

        Page<Team> teams = teamService.searchTeams(
                page,
                pageSize,
                name,
                null
        );

        addGlobalValues(principal, model, "Administration - Groupes", null);
        model.addAttribute("teams", teams.getContent());
        model.addAttribute("matches", teams.getTotalElements());
        model.addAttribute("pages", teams.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);

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

    /**
     * Annulation de la bascule vers Pedalons (ou degel d'une migration bloquee) : le groupe
     * redevient un groupe biketeam ordinaire. Les navigateurs ayant mis un 301 en cache
     * continueront d'aller sur Pedalons.
     */
    @PostMapping(value = "/pedalons-unswitch/{teamId}")
    public String unswitchPedalons(@PathVariable("teamId") String teamId,
                                   RedirectAttributes attributes) {

        final Team team = checkTeam(teamId);
        pedalonsMigrationService.unswitch(team.getId());
        attributes.addFlashAttribute("infos", List.of("Le groupe " + team.getId() + " n'est plus redirigé vers Pédalons."));

        return "redirect:/admin/teams";
    }

}
