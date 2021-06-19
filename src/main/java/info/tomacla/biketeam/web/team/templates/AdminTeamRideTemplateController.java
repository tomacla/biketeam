package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.RideTemplateService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/templates")
public class AdminTeamRideTemplateController extends AbstractController {

    @Autowired
    private RideTemplateService rideTemplateService;


    @GetMapping
    public String getTemplates(@PathVariable("teamId") String teamId,
                               Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        addGlobalValues(principal, model, "Administration - Templates", team);
        model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
        return "team_admin_templates";
    }

    @GetMapping(value = "/new")
    public String newTemplate(@PathVariable("teamId") String teamId,
                              Principal principal,
                              Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        NewRideTemplateForm form = NewRideTemplateForm.builder(1).get();

        addGlobalValues(principal, model, "Administration - Nouveau template", team);
        model.addAttribute("formdata", form);
        return "team_admin_templates_new";

    }

    @GetMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("teamId") String teamId,
                               @PathVariable("templateId") String templateId,
                               Principal principal,
                               Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        Optional<RideTemplate> optionalTemplate = rideTemplateService.get(team.getId(), templateId);
        if (optionalTemplate.isEmpty()) {
            return redirectToAdminTemplates(team.getId());
        }

        RideTemplate rideTemplate = optionalTemplate.get();
        NewRideTemplateForm form = NewRideTemplateForm.builder(rideTemplate.getGroups().size())
                .withId(rideTemplate.getId())
                .withDescription(rideTemplate.getDescription())
                .withName(rideTemplate.getName())
                .withType(rideTemplate.getType())
                .withGroups(rideTemplate.getGroups())
                .get();


        addGlobalValues(principal, model, "Administration - Modifier le template", team);
        model.addAttribute("formdata", form);
        return "team_admin_templates_new";

    }

    @PostMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("teamId") String teamId,
                               @PathVariable("templateId") String templateId,
                               Principal principal,
                               Model model,
                               NewRideTemplateForm form) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {

            boolean isNew = templateId.equals("new");

            NewRideTemplateForm.NewRideTemplateFormParser parser = form.parser();
            RideTemplate target;
            if (!isNew) {
                Optional<RideTemplate> optionalTemplate = rideTemplateService.get(team.getId(), templateId);
                if (optionalTemplate.isEmpty()) {
                    return redirectToAdminTemplates(team.getId());
                }
                target = optionalTemplate.get();
                target.setName(parser.getName());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
            } else {
                target = new RideTemplate(team.getId(), parser.getName(), parser.getType(), parser.getDescription(), null);
            }

            target.clearGroups();
            parser.getGroups().forEach(target::addGroup);

            rideTemplateService.save(target);

            addGlobalValues(principal, model, "Administration - Templates", team);
            model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
            return "team_admin_templates";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le template", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_templates_new";
        }

    }

    @GetMapping(value = "/delete/{templateId}")
    public String deleteTemplate(@PathVariable("teamId") String teamId,
                                 @PathVariable("templateId") String templateId,
                                 Principal principal,
                                 Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {
            rideTemplateService.delete(team.getId(), templateId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminTemplates(team.getId());

    }

    private String redirectToAdminTemplates(String teamId) {
        return "redirect:/" + teamId + "/admin/templates";
    }

}
