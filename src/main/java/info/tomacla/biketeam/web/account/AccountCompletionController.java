package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.password.PasswordPolicy;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.auth.ChangePasswordForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Completion forcee d'un compte incomplet (typiquement un compte cree par la connexion Strava).
 * <p>
 * Deux chemins sont proposes : associer une adresse email verifiee et un mot de passe, ou lier
 * le compte a Google ou Facebook (via /account/link/{registrationId}).
 */
@Controller
@RequestMapping(value = "/account/complete")
public class AccountCompletionController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AccountCompletionController.class);

    private static final int MAX_EMAIL_VERIFICATIONS_PER_HOUR = 5;

    @Autowired
    private AccountCompletionService accountCompletionService;

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private AuthMailService authMailService;

    @Autowired
    private SecurityContextService securityContextService;

    @GetMapping(value = {"", "/"})
    public String completePage(Principal principal,
                               Model model,
                               HttpServletRequest request) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        if (user.isAccountComplete()) {
            accountCompletionService.markComplete(request);
            return "redirect:/";
        }

        addGlobalValues(principal, model, "Compléter mon compte", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", CompleteAccountEmailForm.builder().withEmail(user.getEmail()).get());
        model.addAttribute("pendingEmail", userAuthTokenService
                .getPendingTargetEmail(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION)
                .orElse(null));

        return "account_complete";

    }

    @PostMapping(value = "/email")
    public String completeEmail(CompleteAccountEmailForm form,
                                Principal principal,
                                RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        if (user.isEmailVerified() && isRememberMeAuthentication(principal)) {
            // le compte possede deja une adresse prouvee : la remplacer revient a changer son
            // identite de connexion, ce qu'un simple cookie remember-me ne doit pas autoriser.
            // La saisie d'une PREMIERE adresse reste possible depuis une session remember-me :
            // c'est le parcours nominal d'un compte Strava a completer.
            attributes.addFlashAttribute("errors",
                    List.of("Pour modifier votre adresse email, reconnectez-vous."));
            return "redirect:/login";
        }

        final String email;
        try {
            email = form.parser().getEmail();
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
            return "redirect:/account/complete";
        }

        if (!userService.isEmailAvailable(email, user.getId())) {
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return "redirect:/account/complete";
        }

        if (userAuthTokenService.isThrottled(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION, MAX_EMAIL_VERIFICATIONS_PER_HOUR)) {
            attributes.addFlashAttribute("errors", List.of("Trop de demandes. Réessayez dans quelques minutes."));
            return "redirect:/account/complete";
        }

        sendEmailVerification(user, email);

        attributes.addFlashAttribute("infos", List.of("Un email de vérification vient d'être envoyé à " + email + "."));
        return "redirect:/account/complete";

    }

    @PostMapping(value = "/resend")
    public String resend(Principal principal, RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        final String target = userAuthTokenService
                .getPendingTargetEmail(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION)
                .orElse(user.getEmail());

        if (target == null) {
            attributes.addFlashAttribute("errors", List.of("Renseignez d'abord une adresse email."));
            return "redirect:/account/complete";
        }

        if (userAuthTokenService.isThrottled(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION, MAX_EMAIL_VERIFICATIONS_PER_HOUR)) {
            attributes.addFlashAttribute("errors", List.of("Trop de demandes. Réessayez dans quelques minutes."));
            return "redirect:/account/complete";
        }

        if (!userService.isEmailAvailable(target, user.getId())) {
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return "redirect:/account/complete";
        }

        sendEmailVerification(user, target);

        attributes.addFlashAttribute("infos", List.of("Un email de vérification vient d'être envoyé à " + target + "."));
        return "redirect:/account/complete";

    }

    /**
     * Pose du premier mot de passe.
     * <p>
     * Interdit tant que l'adresse n'est pas verifiee : sans cela, quiconque disposerait de la
     * session d'un compte Strava pourrait se l'approprier definitivement.
     */
    @PostMapping(value = "/password")
    public String completePassword(ChangePasswordForm form,
                                   Principal principal,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        if (!user.isEmailVerified()) {
            attributes.addFlashAttribute("errors", List.of("Vous devez d'abord vérifier votre adresse email."));
            return "redirect:/account/complete";
        }

        final ChangePasswordForm.ChangePasswordFormParser parser = form.parser();

        try {
            PasswordPolicy.validate(parser.getPassword(), parser.getPasswordConfirm(), user.getEmail());
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
            return "redirect:/account/complete";
        }

        userService.setPassword(user.getId(), parser.getPassword());

        // le principal doit imperativement etre reconstruit et le contexte sauvegarde dans cette
        // meme requete, sinon l'utilisateur reste bloque dans la boucle de redirection
        final User refreshed = userService.get(user.getId()).orElse(user);
        securityContextService.refreshCurrentUser(refreshed, request, response);
        accountCompletionService.markComplete(request);

        attributes.addFlashAttribute("infos", List.of("Votre compte est complet. Merci !"));
        return "redirect:/";

    }

    private void sendEmailVerification(User user, String target) {

        try {

            final String clearToken = userAuthTokenService.create(user.getId(),
                    UserAuthTokenType.EMAIL_VERIFICATION, target,
                    userAuthTokenService.getEmailVerificationValidity());

            authMailService.sendEmailVerification(target, clearToken);

            // l'ancienne adresse est prevenue de la demande, jamais silencieusement remplacee
            if (user.getEmail() != null && !user.getEmail().equals(target)) {
                authMailService.sendEmailChangeAlert(user.getEmail(), target);
            }

        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent email verification request", e);
        }

    }

}
