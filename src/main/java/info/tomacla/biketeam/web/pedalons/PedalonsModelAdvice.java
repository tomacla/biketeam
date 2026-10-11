package info.tomacla.biketeam.web.pedalons;

import info.tomacla.biketeam.service.pedalons.PedalonsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose aux templates l'activation de la migration vers Pedalons et le masquage de l'entree de
 * menu de l'administration du groupe : masquee quand la fonction est eteinte, et toujours quand
 * PEDALONS_MENU_HIDDEN=true (la page reste alors accessible par son URL).
 */
@ControllerAdvice
public class PedalonsModelAdvice {

    @Autowired
    private PedalonsProperties pedalonsProperties;

    @Value("${pedalons.menu-hidden:false}")
    private boolean menuHidden;

    @ModelAttribute("_pedalonsEnabled")
    public boolean pedalonsEnabled() {
        return pedalonsProperties.isEnabled();
    }

    @ModelAttribute("_pedalonsMenuHidden")
    public boolean pedalonsMenuHidden() {
        return menuHidden;
    }

}
