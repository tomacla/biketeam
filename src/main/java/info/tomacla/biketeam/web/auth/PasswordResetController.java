package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.password.PasswordPolicy;
import info.tomacla.biketeam.security.session.UserSessionService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Mot de passe oublie et reinitialisation.
 * <p>
 * La reponse de /forgot-password est strictement inconditionnelle : elle ne permet jamais de
 * savoir si une adresse correspond a un compte.
 */
@Controller
public class PasswordResetController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private static final String INVALID_LINK = "Ce lien n'est plus valide. Demandez-en un nouveau.";

    private static final String FORGOT_PASSWORD_ANSWER =
            "Si un compte existe avec cette adresse, un email de réinitialisation vient d'être envoyé.";

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private AuthMailService authMailService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private UserSessionService userSessionService;

    @Value("${auth.forgot-password.max-per-hour:3}")
    private int maxForgotPasswordPerHour;

    @GetMapping(value = "/forgot-password")
    public String forgotPasswordPage(Principal principal, Model model) {
        addGlobalValues(principal, model, "Mot de passe oublié", null);
        model.addAttribute("formdata", ForgotPasswordForm.builder().get());
        return "forgot_password";
    }

    @PostMapping(value = "/forgot-password")
    public String forgotPassword(ForgotPasswordForm form,
                                 Principal principal,
                                 Model model,
                                 HttpServletRequest request,
                                 RedirectAttributes attributes) {

        final String email;
        try {
            email = form.parser().getEmail();
        } catch (IllegalArgumentException e) {
            addGlobalValues(principal, model, "Mot de passe oublié", null);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "forgot_password";
        }

        // double limitation : par adresse visee et par origine de la requete.
        // le & non court-circuitant est VOLONTAIRE : les deux compteurs doivent enregistrer la
        // tentative, sinon un attaquant echappe au comptage par IP en saturant une seule adresse.
        final boolean allowed =
                rateLimitService.tryAcquire("forgot-password:" + email, maxForgotPasswordPerHour)
                        & rateLimitService.tryAcquire(rateLimitService.clientKey(request, "forgot-password"),
                        maxForgotPasswordPerHour * 5);

        if (allowed) {
            try {
                userService.getByEmail(email).ifPresent(user -> {
                    final String clearToken = userAuthTokenService.create(user.getId(),
                            UserAuthTokenType.PASSWORD_RESET, email,
                            userAuthTokenService.getPasswordResetValidity());
                    authMailService.sendPasswordReset(email, clearToken);
                });
            } catch (Exception e) {
                log.error("Unable to send password reset email", e);
            }
        }

        // reponse identique quel que soit le resultat, y compris en cas de limitation de debit
        attributes.addFlashAttribute("infos", List.of(FORGOT_PASSWORD_ANSWER));
        return "redirect:/forgot-password";

    }

    @GetMapping(value = "/reset-password")
    public String resetPasswordPage(@RequestParam(value = "code", required = false) String code,
                                    Principal principal,
                                    RedirectAttributes attributes,
                                    Model model) {

        // le token n'est consomme qu'a la soumission, mais inutile d'afficher un formulaire
        // dont on sait deja que l'envoi sera refuse
        if (!userAuthTokenService.isUsable(code, UserAuthTokenType.PASSWORD_RESET)) {
            attributes.addFlashAttribute("errors", List.of(INVALID_LINK));
            return "redirect:/forgot-password";
        }

        addGlobalValues(principal, model, "Nouveau mot de passe", null);
        model.addAttribute("formdata", ResetPasswordForm.builder().withCode(code).get());
        return "reset_password";

    }

    @PostMapping(value = "/reset-password")
    public String resetPassword(ResetPasswordForm form,
                                Principal principal,
                                Model model,
                                RedirectAttributes attributes) {

        final String code;
        try {

            final ResetPasswordForm.ResetPasswordFormParser parser = form.parser();
            code = parser.getCode();

            // controles ne dependant pas du compte (longueur, confirmation) : ils sont faits
            // AVANT la consommation du token pour ne pas bruler le lien sur une simple faute
            // de frappe dans la confirmation
            PasswordPolicy.validate(parser.getPassword(), parser.getPasswordConfirm(), null);

        } catch (IllegalArgumentException e) {
            return renderResetError(principal, model, form, e.getMessage());
        }

        final Optional<UserAuthToken> optionalToken =
                userAuthTokenService.consume(code, UserAuthTokenType.PASSWORD_RESET);

        if (optionalToken.isEmpty()) {
            return renderResetError(principal, model, form, INVALID_LINK);
        }

        final Optional<User> optionalUser = userService.get(optionalToken.get().getUserId());
        if (optionalUser.isEmpty()) {
            return renderResetError(principal, model, form, INVALID_LINK);
        }

        final User user = optionalUser.get();
        final String rawPassword = form.getPassword();

        try {
            PasswordPolicy.validate(rawPassword, form.getPasswordConfirm(), user.getEmail());
        } catch (IllegalArgumentException e) {
            // le lien vient d'etre consomme : on en emet immediatement un nouveau pour ne pas
            // laisser l'utilisateur sans moyen de recuperation
            return renderResetError(principal, model, form, e.getMessage() + " " + reissue(user));
        }

        // la reception du mail prouve le controle de la boite : l'adresse devient verifiee
        final String verifiedEmail = optionalToken.get().getTargetEmail() != null
                ? optionalToken.get().getTargetEmail()
                : user.getEmail();

        if (verifiedEmail != null && userService.isEmailAvailable(verifiedEmail, user.getId())) {
            user.setVerifiedEmail(verifiedEmail);
            userService.save(user);
        }

        userService.setPassword(user.getId(), rawPassword);

        // compte potentiellement compromis : cookies remember-me ET sessions HTTP deja ouvertes
        // sont invalides. La rotation de la graine ne suffit pas : une session persistee en base
        // n'est jamais reconfrontee a user_account, elle doit etre supprimee explicitement.
        userService.rotateAuthTokenSeed(user.getId());
        userSessionService.invalidateAll(user.getId());
        userAuthTokenService.invalidateAll(user.getId(), UserAuthTokenType.PASSWORD_RESET);

        attributes.addFlashAttribute("infos",
                List.of("Votre mot de passe a été modifié. Vous pouvez vous connecter."));
        return "redirect:/login";

    }

    private String reissue(User user) {
        try {
            final String clearToken = userAuthTokenService.create(user.getId(),
                    UserAuthTokenType.PASSWORD_RESET, user.getEmail(),
                    userAuthTokenService.getPasswordResetValidity());
            authMailService.sendPasswordReset(user.getEmail(), clearToken);
            return "Un nouveau lien vient de vous être envoyé.";
        } catch (Exception e) {
            log.error("Unable to reissue password reset token", e);
            return INVALID_LINK;
        }
    }

    /**
     * Reaffiche le formulaire sans jamais reinjecter le mot de passe saisi.
     */
    private String renderResetError(Principal principal, Model model, ResetPasswordForm form, String message) {
        addGlobalValues(principal, model, "Nouveau mot de passe", null);
        model.addAttribute("errors", List.of(message));
        model.addAttribute("formdata", ResetPasswordForm.builder().withCode(form.getCode()).get());
        return "reset_password";
    }

}
