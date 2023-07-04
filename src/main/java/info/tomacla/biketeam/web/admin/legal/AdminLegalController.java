package info.tomacla.biketeam.web.admin.legal;

import info.tomacla.biketeam.domain.parameter.Parameter;
import info.tomacla.biketeam.domain.parameter.ParameterRepository;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.team.configuration.EditTeamFAQForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/admin/legal")
public class AdminLegalController extends AbstractController {

    @Autowired
    private ParameterRepository parameterRepository;

    @GetMapping
    public String getLegalMentions(Principal principal, Model model) {

        Optional<Parameter> optionalLegalMentions = parameterRepository.findById("LEGAL_MENTIONS");

        EditTeamFAQForm form = EditTeamFAQForm.builder()
                .withMarkdownPage(optionalLegalMentions.isPresent() ? optionalLegalMentions.get().getValue() : "")
                .get();


        addGlobalValues(principal, model, "Administration - Mentions légales", null);
        model.addAttribute("formdata", form);

        return "admin_legal";
    }

    @PostMapping
    public String updateLegalMentions(Principal principal, Model model,
                                      RedirectAttributes attributes,
                                      EditTeamFAQForm form) {

        final EditTeamFAQForm.EditTeamPageFormParser parser = form.parser();

        Optional<Parameter> optionalLegalMentions = parameterRepository.findById("LEGAL_MENTIONS");
        Parameter target = optionalLegalMentions.orElse(new Parameter());
        target.setName("LEGAL_MENTIONS");
        target.setValue(parser.getMarkdownPage());

        parameterRepository.save(target);

        addGlobalValues(principal, model, "Administration - Mentions légales", null);
        model.addAttribute("formdata", form);
        return "admin_legal";

    }

}
