package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.RideTemplateService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
                               @ModelAttribute("error") String error,
                               Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Templates", team);
        model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_templates";
    }

    @GetMapping(value = "/new")
    public String newTemplate(@PathVariable("teamId") String teamId,
                              @ModelAttribute("error") String error,
                              Principal principal,
                              Model model) {

        final Team team = checkTeam(teamId);

        NewRideTemplateForm form = NewRideTemplateForm.builder(1).get();

        addGlobalValues(principal, model, "Administration - Nouveau template", team);
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_templates_new";

    }

    @GetMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("teamId") String teamId,
                               @PathVariable("templateId") String templateId,
                               @ModelAttribute("error") String error,
                               Principal principal,
                               Model model) {

        final Team team = checkTeam(teamId);

        Optional<RideTemplate> optionalTemplate = rideTemplateService.get(team.getId(), templateId);
        if (optionalTemplate.isEmpty()) {
            return viewHandler.redirect(team, "/admin/templates");
        }

        RideTemplate rideTemplate = optionalTemplate.get();
        NewRideTemplateForm form = NewRideTemplateForm.builder(rideTemplate.getGroups().size())
                .withId(rideTemplate.getId())
                .withDescription(rideTemplate.getDescription())
                .withName(rideTemplate.getName())
                .withIncrement(rideTemplate.getIncrement())
                .withType(rideTemplate.getType())
                .withGroups(rideTemplate.getSortedGroups())
                .get();


        addGlobalValues(principal, model, "Administration - Modifier le template", team);
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_templates_new";

    }

    @PostMapping(value = "/{templateId}")
    public RedirectView editTemplate(@PathVariable("teamId") String teamId,
                                     @PathVariable("templateId") String templateId,
                                     RedirectAttributes attributes,
                                     Principal principal,
                                     Model model,
                                     NewRideTemplateForm form) {

        final Team team = checkTeam(teamId);

        try {

            boolean isNew = templateId.equals("new");

            NewRideTemplateForm.NewRideTemplateFormParser parser = form.parser();
            RideTemplate target;
            if (!isNew) {
                Optional<RideTemplate> optionalTemplate = rideTemplateService.get(team.getId(), templateId);
                if (optionalTemplate.isEmpty()) {
                    return viewHandler.redirectView(team, "/admin/templates");
                }
                target = optionalTemplate.get();
                target.setName(parser.getName());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
            } else {
                target = new RideTemplate();
                target.setTeamId(team.getId());
                target.setName(parser.getName());
                target.setType(parser.getType());
                target.setDescription(parser.getDescription());
            }

            target.setIncrement(parser.getIncrement());

            target.clearGroups();
            parser.getGroups().forEach(target::addGroup);

            rideTemplateService.save(target);

            return viewHandler.redirectView(team, "/admin/templates");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/templates/" + templateId);
        }

    }

    @GetMapping(value = "/delete/{templateId}")
    public RedirectView deleteTemplate(@PathVariable("teamId") String teamId,
                                       @PathVariable("templateId") String templateId,
                                       RedirectAttributes attributes,
                                       Principal principal,
                                       Model model) {

        final Team team = checkTeam(teamId);

        try {
            rideTemplateService.delete(team.getId(), templateId);
            return viewHandler.redirectView(team, "/admin/templates");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/templates");
        }

    }

}
