package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.password.LoginFailureHandler;
import info.tomacla.biketeam.security.password.PasswordPolicy;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Inscription par email et mot de passe.
 * <p>
 * Les trois issues possibles (adresse libre, compte complet existant, compte existant sans mot
 * de passe) produisent exactement la meme reponse : aucune enumeration de comptes n'est possible
 * depuis cette route.
 */
@Controller
@RequestMapping(value = "/register")
public class RegistrationController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private AuthMailService authMailService;

    @Autowired
    private RateLimitService rateLimitService;

    @Value("${auth.register.max-per-hour:5}")
    private int maxRegisterPerHour;

    @GetMapping(value = {"", "/"})
    public String registerPage(Principal principal, Model model) {
        addGlobalValues(principal, model, "Créer un compte", null);
        model.addAttribute("formdata", RegisterForm.builder().get());
        return "register";
    }

    @PostMapping(value = {"", "/"})
    public String register(RegisterForm form,
                           Principal principal,
                           Model model,
                           HttpServletRequest request,
                           HttpSession session) {

        final String firstName;
        final String lastName;
        final String email;

        try {

            final RegisterForm.RegisterFormParser parser = form.parser();

            firstName = parser.getFirstName();
            lastName = parser.getLastName();
            email = parser.getEmail();

            PasswordPolicy.validate(parser.getPassword(), parser.getPasswordConfirm(), email);

        } catch (IllegalArgumentException e) {
            return renderError(principal, model, form, e.getMessage());
        }

        if (!rateLimitService.tryAcquire(rateLimitService.clientKey(request, "register"), maxRegisterPerHour)) {
            return renderError(principal, model, form,
                    "Trop de demandes depuis cette connexion. Réessayez dans quelques minutes.");
        }

        try {
            handleRegistration(firstName, lastName, email, form.getPassword());
        } catch (DataIntegrityViolationException e) {
            // course entre deux inscriptions sur la meme adresse : l'index unique fonctionnel a
            // tranche. La reponse reste identique aux autres cas.
            log.warn("Concurrent registration on an already used email address");
        } catch (Exception e) {
            log.error("Unable to complete registration", e);
        }

        session.setAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL, email);

        return "redirect:/register/pending";

    }

    @GetMapping(value = "/pending")
    public String pending(Principal principal, Model model, HttpSession session) {

        final Object pendingEmail = session.getAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL);
        if (pendingEmail == null) {
            return "redirect:/login";
        }

        addGlobalValues(principal, model, "Confirmez votre adresse email", null);
        model.addAttribute("pendingEmail", pendingEmail);
        return "register_pending";

    }

    @PostMapping(value = "/resend")
    public String resend(HttpServletRequest request,
                         HttpSession session,
                         RedirectAttributes attributes) {

        final Object pendingEmail = session.getAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL);
        if (pendingEmail == null) {
            return "redirect:/login";
        }

        final String email = String.valueOf(pendingEmail);

        if (!rateLimitService.tryAcquire(rateLimitService.clientKey(request, "register-resend"), maxRegisterPerHour)) {
            attributes.addFlashAttribute("errors",
                    List.of("Trop de demandes depuis cette connexion. Réessayez dans quelques minutes."));
            return "redirect:/register/pending";
        }

        try {
            userService.getByEmail(email)
                    .filter(user -> !user.isEmailVerified())
                    .ifPresent(user -> {
                        final String clearToken = userAuthTokenService.create(user.getId(),
                                UserAuthTokenType.EMAIL_VERIFICATION, email,
                                userAuthTokenService.getEmailVerificationValidity());
                        authMailService.sendEmailVerification(email, clearToken);
                    });
        } catch (Exception e) {
            log.error("Unable to resend email verification", e);
        }

        // message inconditionnel : le renvoi ne revele jamais l'existence du compte
        attributes.addFlashAttribute("infos", List.of("Un nouvel email vient d'être envoyé à " + email + "."));
        return "redirect:/register/pending";

    }

    /**
     * Les trois cas de la specification. Aucun ne cree de second compte sur une adresse deja
     * utilisee, et le mot de passe saisi n'est jamais pose sur un compte preexistant : ce serait
     * une prise de controle immediate de tout compte Strava dont l'adresse est connue.
     */
    private void handleRegistration(String firstName, String lastName, String email, String rawPassword) {

        final Optional<User> existing = userService.getByEmail(email);

        if (existing.isEmpty()) {

            log.info("Registering new user with email login");

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);

            final User saved = userService.save(user);
            userService.setPassword(saved.getId(), rawPassword);

            final String clearToken = userAuthTokenService.create(saved.getId(),
                    UserAuthTokenType.EMAIL_VERIFICATION, email,
                    userAuthTokenService.getEmailVerificationValidity());
            authMailService.sendEmailVerification(email, clearToken);

            return;

        }

        final User user = existing.get();

        if (user.hasPasswordLogin()) {
            // compte deja complet : on previent le titulaire, sans rien modifier
            authMailService.sendAccountAlreadyExists(email);
            return;
        }

        // compte existant sans mot de passe ou non verifie (cas de migration le plus frequent :
        // compte Strava dont l'adresse a ete saisie a la main). On propose de definir un mot de
        // passe sur le compte existant, qui conserve ainsi ses equipes et son historique.
        final String clearToken = userAuthTokenService.create(user.getId(),
                UserAuthTokenType.PASSWORD_RESET, email,
                userAuthTokenService.getPasswordResetValidity());
        authMailService.sendDefinePassword(email, clearToken);

    }

    /**
     * Reaffiche le formulaire sans jamais reinjecter le mot de passe saisi.
     */
    private String renderError(Principal principal, Model model, RegisterForm form, String message) {
        addGlobalValues(principal, model, "Créer un compte", null);
        model.addAttribute("errors", List.of(message));
        model.addAttribute("formdata", RegisterForm.builder()
                .withFirstName(form.getFirstName())
                .withLastName(form.getLastName())
                .withEmail(form.getEmail())
                .get());
        return "register";
    }

}
