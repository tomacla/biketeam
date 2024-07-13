package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.publication.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/confirm-email")
public class ConfirmEmailController extends AbstractController {

    @Autowired
    private PublicationRepository publicationRepository;

    @GetMapping
    public String confirmEmail(@RequestParam(value = "code", required = true) String code,
                               @ModelAttribute("error") String error,
                               Principal principal,
                               RedirectAttributes attributes,
                               Model model) {

        int result = publicationRepository.validateEmail(code);

        List<String> errors = new ArrayList<>();
        if (!ObjectUtils.isEmpty(error)) {
            errors.add(error);
        }
        if (result > 0) {
            attributes.addFlashAttribute("infos", List.of("Votre adresse email est bien confirmée."));
        } else {
            errors.add("Impossible de confirmer cette adresse email");
        }

        model.addAttribute("errors", List.of(error));

        return "redirect:/";

    }


}
