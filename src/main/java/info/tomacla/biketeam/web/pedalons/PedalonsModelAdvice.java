package info.tomacla.biketeam.web.pedalons;

import info.tomacla.biketeam.service.pedalons.PedalonsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose aux templates l'activation de la migration vers Pedalons, pour masquer l'entree de
 * menu de l'administration du groupe quand la fonction est eteinte.
 */
@ControllerAdvice
public class PedalonsModelAdvice {

    @Autowired
    private PedalonsProperties pedalonsProperties;

    @ModelAttribute("_pedalonsEnabled")
    public boolean pedalonsEnabled() {
        return pedalonsProperties.isEnabled();
    }

}
