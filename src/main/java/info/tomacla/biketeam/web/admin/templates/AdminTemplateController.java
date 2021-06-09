package info.tomacla.biketeam.web.admin.templates;

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
@RequestMapping(value = "/admin/templates")
public class AdminTemplateController extends AbstractController {

    @Autowired
    private RideTemplateService rideTemplateService;


    @GetMapping
    public String getTemplates(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Templates");
        model.addAttribute("templates", rideTemplateService.listTemplates());
        return "admin_templates";
    }

    @GetMapping(value = "/new")
    public String newTemplate(Principal principal,
                              Model model) {

        NewRideTemplateForm form = NewRideTemplateForm.builder(1).get();

        addGlobalValues(principal, model, "Administration - Nouveau template");
        model.addAttribute("formdata", form);
        return "admin_templates_new";

    }

    @GetMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("templateId") String templateId,
                               Principal principal,
                               Model model) {

        Optional<RideTemplate> optionalTemplate = rideTemplateService.get(templateId);
        if (optionalTemplate.isEmpty()) {
            return "redirect:/admin/templates";
        }

        RideTemplate rideTemplate = optionalTemplate.get();
        NewRideTemplateForm form = NewRideTemplateForm.builder(rideTemplate.getGroups().size())
                .withId(rideTemplate.getId())
                .withDescription(rideTemplate.getDescription())
                .withName(rideTemplate.getName())
                .withType(rideTemplate.getType())
                .withGroups(rideTemplate.getGroups())
                .get();


        addGlobalValues(principal, model, "Administration - Modifier le template");
        model.addAttribute("formdata", form);
        return "admin_templates_new";

    }

    @PostMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("templateId") String templateId,
                               Principal principal,
                               Model model,
                               NewRideTemplateForm form) {

        try {

            boolean isNew = templateId.equals("new");

            NewRideTemplateForm.NewRideTemplateFormParser parser = form.parser();
            RideTemplate target;
            if (!isNew) {
                Optional<RideTemplate> optionalTemplate = rideTemplateService.get(templateId);
                if (optionalTemplate.isEmpty()) {
                    return "redirect:/admin/templates";
                }
                target = optionalTemplate.get();
                target.setName(parser.getName());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
            } else {
                target = new RideTemplate(parser.getName(), parser.getType(), parser.getDescription());
            }

            target.clearGroups();
            parser.getGroups().forEach(target::addGroup);

            rideTemplateService.save(target);

            addGlobalValues(principal, model, "Administration - Templates");
            model.addAttribute("templates", rideTemplateService.listTemplates());
            return "admin_templates";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le template");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_templates_new";
        }

    }

    @GetMapping(value = "/delete/{templateId}")
    public String deleteTemplate(@PathVariable("templateId") String templateId,
                                 Model model) {

        try {
            rideTemplateService.delete(templateId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/templates";

    }

}
