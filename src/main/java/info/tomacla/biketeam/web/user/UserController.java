package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.security.passkey.PasskeyService;
import info.tomacla.biketeam.security.password.PasswordPolicy;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.auth.ChangeEmailForm;
import info.tomacla.biketeam.web.auth.ChangePasswordForm;
import info.tomacla.biketeam.web.team.NewTeamForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static final int MAX_EMAIL_VERIFICATIONS_PER_HOUR = 5;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private AuthMailService authMailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasskeyService passkeyService;

    @GetMapping(value = "/me")
    public String getUser(Principal principal,
                          Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        addUserModel(principal, model, user);
        return "user";


    }

    /**
     * Valeurs communes aux rendus de /users/me.
     */
    private void addUserModel(Principal principal, Model model, User user) {

        final EditUserForm form = EditUserForm.builder()
                .withEmailPublishPublications(user.isEmailPublishPublications())
                .withEmailPublishRides(user.isEmailPublishRides())
                .withEmailPublishTrips(user.isEmailPublishTrips())
                .get();

        addGlobalValues(principal, model, "Mon profil", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", form);
        model.addAttribute("pendingEmail", userAuthTokenService
                .getPendingTargetEmail(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION)
                .orElse(null));
        model.addAttribute("passkeys", passkeyService.listByUser(user.getId()));

    }

    @GetMapping(value = "/space")
    public String getSpace(Principal principal,
                           Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        if (user.getTeamId() != null) {
            return "redirect:/" + user.getTeamId();
        }

        addGlobalValues(principal, model, "Créer mon espace", null);
        model.addAttribute("formdata", NewTeamForm.builder()
                .withCity(user.getCity())
                .withCountry(Country.FR.name())
                .withName(user.getIdentity())
                .withId(teamService.getPermalink(user.getIdentity(), 18, true))
                .withTimezone(Timezone.DEFAULT_TIMEZONE)
                .withDescription("Espace personnel de " + user.getIdentity())
                .get());
        model.addAttribute("timezones", getAllAvailableTimeZones());
        return "user_space";


    }

    @PostMapping(value = "/space")
    public String initSpace(NewTeamForm form, Principal principal, Model model,
                            HttpServletRequest request, HttpServletResponse response) {

        try {

            final User targetAdmin = getUserFromPrincipal(principal).orElseThrow(() -> new IllegalStateException("User not authenticated"));

            final NewTeamForm.NewTeamFormParser parser = form.parser();

            String targetId = parser.getId().toLowerCase();

            teamService.get(targetId).ifPresent(team -> {
                throw new IllegalArgumentException("Team " + team.getId() + " already exists");
            });

            final Team newTeam = new Team();
            newTeam.markAsNew();

            newTeam.setId(targetId);
            newTeam.setName(parser.getName());
            newTeam.setCity(parser.getCity());
            newTeam.setCountry(parser.getCountry());
            newTeam.getDescription().setDescription(parser.getDescription());
            newTeam.getConfiguration().setTimezone(parser.getTimezone());
            newTeam.setVisibility(Visibility.USER);

            teamService.save(newTeam);

            userRoleService.save(new UserRole(newTeam, targetAdmin, Role.ADMIN));

            targetAdmin.setTeamId(targetId);
            userService.save(targetAdmin);

            addAuthorityToCurrentSession(Authorities.teamAdmin(newTeam.getId()), request, response);

            return "redirect:/" + newTeam.getId();

        } catch (Exception e) {
            addGlobalValues(principal, model, "Créer mon espace", null);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            return "user_space";
        }


    }


    /**
     * Suppression du compte.
     * <p>
     * En POST et sous protection CSRF : une operation destructrice (deletion, effacement du hash
     * de mot de passe, rotation de la graine remember-me) ne doit jamais etre declenchable par une
     * simple navigation provoquee depuis un site tiers, d'autant que la route est volontairement
     * en liste blanche du filtre de completion de compte.
     */
    @PostMapping(value = "/me/delete")
    public String deleteMyself(Principal principal, Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        userService.delete(user.getId());

        return "redirect:/logout";

    }

    @PostMapping(value = "/me")
    public String updateUser(Principal principal,
                             Model model,
                             EditUserForm form) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final EditUserForm.EditUserFormParser parser = form.parser();

        final User user = optionalConnectedUser.get();
        // ni l'email ni le strava id ne sont modifiables ici : l'adresse est une identite de
        // connexion, elle passe par /users/me/email et un lien de verification
        user.setEmailPublishPublications(parser.isEmailPublishPublications());
        user.setEmailPublishRides(parser.isEmailPublishRides());
        user.setEmailPublishTrips(parser.isEmailPublishTrips());
        userService.save(user);

        addUserModel(principal, model, user);
        return "user";


    }

    /**
     * Definition ou changement du mot de passe.
     * <p>
     * Le mot de passe actuel est exige des lors qu'il en existe un. Aucune rotation de la graine
     * remember-me n'est faite ici : le changement est volontaire et authentifie, l'utilisateur ne
     * doit pas etre deconnecte de ses autres appareils.
     */
    @PostMapping(value = "/me/password")
    public String updatePassword(Principal principal,
                                 ChangePasswordForm form,
                                 RedirectAttributes attributes) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();
        final ChangePasswordForm.ChangePasswordFormParser parser = form.parser();

        try {

            if (user.getPasswordHash() != null) {

                final String currentPassword = parser.getCurrentPassword();
                if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                    throw new IllegalArgumentException("Le mot de passe actuel est incorrect.");
                }

            } else {

                if (!user.isEmailVerified()) {
                    // premier mot de passe : l'adresse doit avoir ete prouvee au prealable, sinon
                    // n'importe qui disposant de la session pourrait s'approprier le compte
                    throw new IllegalArgumentException("Vous devez d'abord vérifier votre adresse email.");
                }

                if (isRememberMeAuthentication(principal)) {
                    // aucun mot de passe actuel a opposer : un cookie remember-me vole ne doit pas
                    // suffire a poser le premier mot de passe du compte
                    attributes.addFlashAttribute("errors",
                            List.of("Pour définir un mot de passe, reconnectez-vous."));
                    return "redirect:/login";
                }

            }

            PasswordPolicy.validate(parser.getPassword(), parser.getPasswordConfirm(), user.getEmail());

        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
            return "redirect:/users/me";
        }

        userService.setPassword(user.getId(), parser.getPassword());

        attributes.addFlashAttribute("infos", List.of("Votre mot de passe a été enregistré."));
        return "redirect:/users/me";

    }

    /**
     * Demande de changement d'adresse email.
     * <p>
     * user_account.email n'est PAS modifie : l'ancienne adresse reste l'identite de connexion
     * jusqu'a confirmation de la nouvelle par le lien envoye.
     */
    @PostMapping(value = "/me/email")
    public String updateEmail(Principal principal,
                              ChangeEmailForm form,
                              RedirectAttributes attributes) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        final String email;
        final String currentPassword;
        try {
            final ChangeEmailForm.ChangeEmailFormParser parser = form.parser();
            email = parser.getEmail();
            currentPassword = parser.getCurrentPassword();
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
            return "redirect:/users/me";
        }

        // l'adresse est une identite de connexion : sa modification exige une preuve de
        // possession, exactement comme le changement de mot de passe
        if (user.getPasswordHash() != null) {

            if (currentPassword == null || currentPassword.isEmpty()
                    || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                attributes.addFlashAttribute("errors", List.of("Le mot de passe actuel est incorrect."));
                return "redirect:/users/me";
            }

        } else if (isRememberMeAuthentication(principal)) {

            // aucun mot de passe a opposer : on exige au moins une authentification fraiche
            attributes.addFlashAttribute("errors",
                    List.of("Pour modifier votre adresse email, reconnectez-vous."));
            return "redirect:/login";

        }

        if (email.equals(user.getEmail()) && user.isEmailVerified()) {
            attributes.addFlashAttribute("infos", List.of("Cette adresse email est déjà vérifiée."));
            return "redirect:/users/me";
        }

        if (!userService.isEmailAvailable(email, user.getId())) {
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return "redirect:/users/me";
        }

        if (userAuthTokenService.isThrottled(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION, MAX_EMAIL_VERIFICATIONS_PER_HOUR)) {
            attributes.addFlashAttribute("errors", List.of("Trop de demandes. Réessayez dans quelques minutes."));
            return "redirect:/users/me";
        }

        sendEmailVerification(user, email);

        attributes.addFlashAttribute("infos", List.of("Un email de vérification vient d'être envoyé à " + email + "."));
        return "redirect:/users/me";

    }

    /**
     * Renvoi du lien de verification en cours (ou, a defaut, de l'adresse actuelle non verifiee).
     */
    @PostMapping(value = "/me/email/resend")
    public String resendEmailVerification(Principal principal, RedirectAttributes attributes) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        final String target = userAuthTokenService
                .getPendingTargetEmail(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION)
                .orElse(user.getEmail());

        if (target == null) {
            attributes.addFlashAttribute("errors", List.of("Renseignez d'abord une adresse email."));
            return "redirect:/users/me";
        }

        if (userAuthTokenService.isThrottled(user.getId(), UserAuthTokenType.EMAIL_VERIFICATION, MAX_EMAIL_VERIFICATIONS_PER_HOUR)) {
            attributes.addFlashAttribute("errors", List.of("Trop de demandes. Réessayez dans quelques minutes."));
            return "redirect:/users/me";
        }

        if (!userService.isEmailAvailable(target, user.getId())) {
            attributes.addFlashAttribute("errors", List.of("Cette adresse email est déjà utilisée par un autre compte."));
            return "redirect:/users/me";
        }

        sendEmailVerification(user, target);

        attributes.addFlashAttribute("infos", List.of("Un email de vérification vient d'être envoyé à " + target + "."));
        return "redirect:/users/me";

    }

    /**
     * Deliaison d'un compte externe.
     * <p>
     * Refusee des lors qu'elle laisserait le compte sans aucun moyen de connexion : Strava est une
     * connexion de transition, elle ne compte pas comme identite de repli.
     */
    @PostMapping(value = "/me/unlink/{provider}")
    public String unlinkProvider(@PathVariable("provider") String provider,
                                 Principal principal,
                                 RedirectAttributes attributes) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        final boolean google = "google".equals(provider);
        final boolean facebook = "facebook".equals(provider);

        if (!google && !facebook) {
            attributes.addFlashAttribute("errors", List.of("Ce fournisseur ne peut pas être délié."));
            return "redirect:/users/me";
        }

        final boolean remainingIdentity = user.hasPasswordLogin()
                || (google ? user.getFacebookId() != null : user.getGoogleId() != null);

        if (!remainingIdentity) {
            attributes.addFlashAttribute("errors",
                    List.of("Vous ne pouvez pas délier ce compte : ce serait votre seul moyen de connexion."));
            return "redirect:/users/me";
        }

        if (google) {
            user.setGoogleId(null);
        } else {
            user.setFacebookId(null);
        }

        userService.save(user);

        attributes.addFlashAttribute("infos", List.of("Le compte externe a été délié."));
        return "redirect:/users/me";

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

    @ResponseBody
    @RequestMapping(value = "/{userId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getUserController(@PathVariable("userId") String userId) {
        final Optional<ImageDescriptor> image = userService.getImage(userId);
        if (image.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", image.get().getExtension().getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(userId + image.get().getExtension().getExtension())
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(image.get().getPath()),
                        headers,
                        HttpStatus.OK
                );
            } catch (IOException e) {
                // ignore
            }
        }

        try {

            InputStream resourceAsStream = getClass().getResourceAsStream("/static/css/default-user.png");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", FileExtension.PNG.getMediaType());
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(userId + FileExtension.PNG.getExtension())
                    .build());

            return new ResponseEntity<>(
                    resourceAsStream.readAllBytes(),
                    headers,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user image : " + userId);
        }

    }

}
