package info.tomacla.biketeam.web.passkey;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserPasskey;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationService;
import info.tomacla.biketeam.security.passkey.PasskeyException;
import info.tomacla.biketeam.security.passkey.PasskeyProperties;
import info.tomacla.biketeam.security.passkey.PasskeyRegistrationBody;
import info.tomacla.biketeam.security.passkey.PasskeyRegistrationService;
import info.tomacla.biketeam.security.passkey.PasskeyService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoints passkey.
 * <p>
 * Les trois routes JSON sont appelees par {@code /js/passkey.js} ; la suppression est un
 * formulaire classique, pour rester coherente avec la deliaison des comptes Google/Facebook
 * de /users/me et fonctionner sans JavaScript.
 */
@Controller
public class PasskeyController extends AbstractController {

    @Autowired
    private PasskeyRegistrationService passkeyRegistrationService;

    @Autowired
    private PasskeyAuthenticationService passkeyAuthenticationService;

    @Autowired
    private PasskeyService passkeyService;

    @Autowired
    private PasskeyProperties passkeyProperties;

    @Autowired
    private RateLimitService rateLimitService;

    /**
     * Options de connexion. Route non authentifiee : elle ne revele rien (le challenge est
     * aleatoire et {@code allowCredentials} est vide), mais elle cree une session HTTP, d'ou
     * la limitation de debit par origine.
     */
    @PostMapping(value = "/login/webauthn/options", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> authenticationOptions(HttpServletRequest request) {

        if (!rateLimitService.tryAcquire(rateLimitService.clientKey(request, "passkey-login-options"),
                passkeyProperties.getLoginOptionsMaxPerHour())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Trop de tentatives de connexion. Réessayez dans quelques minutes."));
        }

        return ResponseEntity.ok(passkeyAuthenticationService.createOptions(request));

    }

    /**
     * Options de creation. Reservee a un utilisateur deja connecte : c'est la regle produit,
     * une passkey ne cree jamais de compte.
     */
    @PostMapping(value = "/users/me/passkeys/options", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrationOptions(Principal principal,
                                                                   HttpServletRequest request) {

        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié."));
        }

        return ResponseEntity.ok(passkeyRegistrationService.createOptions(request, optionalUser.get()));

    }

    @PostMapping(value = "/users/me/passkeys", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(Principal principal,
                                                        HttpServletRequest request,
                                                        @RequestBody PasskeyRegistrationBody body) {

        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié."));
        }

        final UserPasskey passkey = passkeyRegistrationService.finish(request, optionalUser.get(), body);

        return ResponseEntity.ok(Map.of("id", passkey.getId(), "label", passkey.getLabel()));

    }

    @PostMapping(value = "/users/me/passkeys/{passkeyId}/delete")
    public String delete(@PathVariable("passkeyId") String passkeyId,
                         Principal principal,
                         RedirectAttributes attributes) {

        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/";
        }

        if (passkeyService.delete(passkeyId, optionalUser.get().getId())) {
            attributes.addFlashAttribute("infos", List.of("La passkey a été supprimée."));
        } else {
            attributes.addFlashAttribute("errors", List.of("Cette passkey est introuvable."));
        }

        return "redirect:/users/me";

    }

    /**
     * Les echecs fonctionnels des routes JSON sont rendus en JSON : le script client affiche le
     * message tel quel. Un 400 plutot qu'un 500 pour ne pas polluer la supervision avec des
     * abandons d'utilisateur (fenetre du navigateur fermee, appareil refuse, delai depasse).
     */
    @ExceptionHandler(PasskeyException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handlePasskeyException(PasskeyException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

}
