package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Consommation d'un lien de verification d'adresse email.
 * <p>
 * Aucune connexion automatique n'est realisee ici : la reception du mail prouve le controle de
 * la boite, pas l'intention de se connecter depuis ce navigateur.
 */
@Controller
@RequestMapping(value = "/verify-email")
public class EmailVerificationController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationController.class);

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private AccountCompletionService accountCompletionService;

    @GetMapping(value = {"", "/"})
    public String verifyEmail(@RequestParam(value = "code", required = false) String code,
                              Principal principal,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              RedirectAttributes attributes) {

        final Optional<User> connectedUser = getUserFromPrincipal(principal);

        final Optional<UserAuthToken> optionalToken =
                userAuthTokenService.consume(code, UserAuthTokenType.EMAIL_VERIFICATION);

        if (optionalToken.isEmpty()) {
            attributes.addFlashAttribute("errors", List.of("Ce lien n'est plus valide. Demandez-en un nouveau."));
            return redirectTarget(connectedUser, null);
        }

        final UserAuthToken token = optionalToken.get();

        final Optional<User> optionalUser = userService.get(token.getUserId());
        if (optionalUser.isEmpty()) {
            attributes.addFlashAttribute("errors", List.of("Ce lien n'est plus valide. Demandez-en un nouveau."));
            return redirectTarget(connectedUser, null);
        }

        final User user = optionalUser.get();
        final String target = token.getTargetEmail() != null ? token.getTargetEmail() : user.getEmail();

        if (target == null) {
            attributes.addFlashAttribute("errors", List.of("Ce lien n'est plus valide. Demandez-en un nouveau."));
            return redirectTarget(connectedUser, user);
        }

        // l'adresse a pu etre verifiee par un tiers entre l'emission du lien et le clic
        if (!userService.isEmailAvailable(target, user.getId())) {
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return redirectTarget(connectedUser, user);
        }

        final User saved;
        try {
            user.setVerifiedEmail(target);
            saved = userService.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent verification of email address", e);
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return redirectTarget(connectedUser, user);
        }

        final boolean self = connectedUser.isPresent() && connectedUser.get().getId().equals(saved.getId());

        if (self) {
            // le principal porte l'etat de completion : il doit refleter l'adresse verifiee des
            // cette requete, sinon l'utilisateur reste bloque par AccountCompletionFilter
            securityContextService.refreshCurrentUser(saved, request, response);
            accountCompletionService.invalidate(request);

            if (saved.getPasswordHash() == null && !saved.hasExternalIdentity()) {
                attributes.addFlashAttribute("infos",
                        List.of("Adresse confirmée. Choisissez maintenant un mot de passe."));
                return "redirect:/account/complete";
            }

            attributes.addFlashAttribute("infos", List.of("Votre adresse email est confirmée."));
            return "redirect:/users/me";
        }

        attributes.addFlashAttribute("infos",
                List.of("Votre adresse email est confirmée. Vous pouvez vous connecter."));
        return "redirect:/login";

    }

    /**
     * Un visiteur non authentifie est renvoye vers la page de connexion ; un utilisateur deja
     * connecte qui vient de confirmer sa propre adresse revient sur son profil.
     */
    private String redirectTarget(Optional<User> connectedUser, User tokenUser) {
        if (connectedUser.isPresent() && tokenUser != null && connectedUser.get().getId().equals(tokenUser.getId())) {
            return "redirect:/users/me";
        }
        return "redirect:/login";
    }

}
