package info.tomacla.biketeam.web;

import info.tomacla.biketeam.service.auth.BotProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose la protection anti-robot aux templates sous le nom {@code _botProtection}.
 * <p>
 * Les formulaires proteges apparaissent sur de nombreuses pages (inscription aux publications
 * dans les fils d'actualite notamment) : plutot que de poser un horodatage dans chaque
 * controleur, la macro {@code botProtection} l'emet elle-meme, et seulement quand elle est
 * rendue.
 */
@ControllerAdvice
public class BotProtectionModelAdvice {

    @Autowired
    private BotProtectionService botProtectionService;

    @ModelAttribute("_botProtection")
    public BotProtectionService botProtection() {
        return botProtectionService;
    }

}
