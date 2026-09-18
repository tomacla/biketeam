package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.service.auth.BotProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Defis ALTCHA des formulaires publics (inscription, mot de passe oublie), recuperes par le
 * widget au moment ou l'utilisateur commence a remplir le formulaire.
 * <p>
 * Le chemin est volontairement neutre : rien n'y designe un captcha aux robots qui parcourent
 * le HTML a la recherche de protections connues.
 */
@RestController
public class AltchaController {

    @Autowired
    private BotProtectionService botProtectionService;

    @GetMapping(value = "/forms/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> challenge() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(botProtectionService.createChallenge());
    }

}
