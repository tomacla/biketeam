package info.tomacla.biketeam.web.admin.templates;

import info.tomacla.biketeam.domain.global.RideGroupTemplate;
import info.tomacla.biketeam.domain.global.RideTemplate;
import info.tomacla.biketeam.domain.global.RideTemplateRepository;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/admin/templates")
public class AdminTemplateController extends AbstractController {

    @Autowired
    private RideTemplateRepository rideTemplateRepository;


    @GetMapping
    public String getTemplates(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Templates");
        model.addAttribute("templates", rideTemplateRepository.findAllByOrderByNameAsc());
        return "admin_templates";
    }

    @GetMapping(value = "/new")
    public String newTemplate(@RequestParam(value = "numberOfGroups", required = false, defaultValue = "1") int numberOfGroups,
                              Principal principal,
                              Model model) {

        NewRideTemplateForm form = NewRideTemplateForm.builder(numberOfGroups).get();

        addGlobalValues(principal, model, "Administration - Nouveau template");
        model.addAttribute("formdata", form);
        return "admin_templates_new";

    }

    @GetMapping(value = "/{templateId}")
    public String editTemplate(@PathVariable("templateId") String templateId,
                               Principal principal,
                               Model model) {

        Optional<RideTemplate> optionalTemplate = rideTemplateRepository.findById(templateId);
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
                Optional<RideTemplate> optionalTemplate = rideTemplateRepository.findById(templateId);
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

            Set<RideGroupTemplate> groups = parser.getGroups(target);
            Set<String> groupIdsSet = groups.stream().map(RideGroupTemplate::getId).collect(Collectors.toSet());

            // remove groups not in list
            target.getGroups().removeIf(g -> !groupIdsSet.contains(g.getId()));
            // update and groups in list
            for (RideGroupTemplate g : groups) {
                Optional<RideGroupTemplate> optionalGroup = target.getGroups().stream().filter(eg -> eg.getId().equals(g.getId())).findFirst();
                if (optionalGroup.isEmpty()) {
                    target.addGroup(new RideGroupTemplate(g.getName(),
                            g.getLowerSpeed(),
                            g.getUpperSpeed(),
                            g.getMeetingLocation(),
                            g.getMeetingTime(),
                            g.getMeetingPoint()));
                } else {
                    RideGroupTemplate toModify = optionalGroup.get();
                    toModify.setLowerSpeed(g.getLowerSpeed());
                    toModify.setUpperSpeed(g.getUpperSpeed());
                    toModify.setMeetingTime(g.getMeetingTime());
                    toModify.setMeetingLocation(g.getMeetingLocation());
                    toModify.setMeetingPoint(g.getMeetingPoint());
                    toModify.setName(g.getName());
                }
            }

            rideTemplateRepository.save(target);

            addGlobalValues(principal, model, "Administration - Templates");
            model.addAttribute("templates", rideTemplateRepository.findAllByOrderByNameAsc());
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
            rideTemplateRepository.findById(templateId).ifPresent(template -> rideTemplateRepository.delete(template));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/templates";

    }

}
