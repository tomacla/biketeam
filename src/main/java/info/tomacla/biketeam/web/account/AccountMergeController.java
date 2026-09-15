package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.service.merge.UserMergeService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Rattachement d'un compte incomplet a un compte existant, sur preuve de possession des deux.
 * <p>
 * C'est l'issue de secours du parcours de completion : un compte cree par Strava n'a pas d'email,
 * et si la personne possede deja un compte email/mot de passe, {@code /account/complete} la
 * laissait auparavant dans une impasse. Strava n'etant pas liable (voir
 * {@link AccountLinkController}), c'est le seul moyen de reunir les deux comptes.
 * <p>
 * <strong>Deux preuves sont exigees, jamais une seule :</strong>
 * <ul>
 * <li>la possession de l'adresse, par le clic sur le lien recu ({@code code}) ;</li>
 * <li>la possession du compte demandeur, par la session ouverte sur lui.</li>
 * </ul>
 * <p>
 * Le sens de la fusion est l'inverse de celui de {@link AccountLinkController} : le compte
 * conserve est celui qui porte une identite de connexion perenne (le compte email), pas celui de
 * la session courante. Il est impose par le token, jamais par le formulaire.
 */
@Controller
@RequestMapping(value = "/account/merge")
public class AccountMergeController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AccountMergeController.class);

    static final String PENDING_MERGE_SOURCE = "PENDING_ACCOUNT_MERGE_REQUEST_SOURCE";
    static final String PENDING_MERGE_TARGET = "PENDING_ACCOUNT_MERGE_REQUEST_TARGET";
    static final String PENDING_MERGE_CREATED_AT = "PENDING_ACCOUNT_MERGE_REQUEST_CREATED_AT";

    /**
     * Le token est consomme des l'affichage de l'ecran de confirmation ; cette fenetre borne le
     * temps dont on dispose ensuite pour confirmer.
     */
    private static final Duration CONFIRMATION_VALIDITY = Duration.ofMinutes(15);

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private UserMergeService userMergeService;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private AccountCompletionService accountCompletionService;

    /**
     * Ouverture du lien recu par email : affiche les deux comptes et ce que la fusion ferait.
     * <p>
     * La session est verifiee AVANT de consommer le token : consommer d'abord brulerait le lien
     * a chaque ouverture depuis le mauvais navigateur, scanners de messagerie compris.
     */
    @GetMapping
    public String confirmPage(@RequestParam(value = "code", required = false) String code,
                              Principal principal,
                              HttpSession session,
                              Model model,
                              RedirectAttributes attributes) {

        final Optional<UserAuthToken> optionalToken =
                userAuthTokenService.peek(code, UserAuthTokenType.ACCOUNT_MERGE);

        if (optionalToken.isEmpty()) {
            attributes.addFlashAttribute("errors",
                    List.of("Ce lien de rattachement est invalide ou a expiré."));
            return "redirect:/login";
        }

        final UserAuthToken token = optionalToken.get();

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()
                || !optionalConnectedUser.get().getId().equals(token.getUserId())) {
            // token volontairement NON consomme : la personne doit pouvoir recliquer depuis le
            // bon navigateur.
            attributes.addFlashAttribute("errors",
                    List.of("Ouvrez ce lien depuis le navigateur où vous êtes connecté avec le compte à rattacher."));
            return "redirect:/login";
        }

        final Optional<User> optionalTargetUser = resolveTarget(token);
        if (optionalTargetUser.isEmpty()) {
            attributes.addFlashAttribute("errors",
                    List.of("Le compte visé par ce lien n'existe plus."));
            return "redirect:/account/complete";
        }

        final User source = optionalConnectedUser.get();
        final User target = optionalTargetUser.get();

        if (source.getId().equals(target.getId())) {
            return "redirect:/account/complete";
        }

        if (userAuthTokenService.consume(code, UserAuthTokenType.ACCOUNT_MERGE).isEmpty()) {
            attributes.addFlashAttribute("errors",
                    List.of("Ce lien de rattachement est invalide ou a expiré."));
            return "redirect:/login";
        }

        session.setAttribute(PENDING_MERGE_SOURCE, source.getId());
        session.setAttribute(PENDING_MERGE_TARGET, target.getId());
        session.setAttribute(PENDING_MERGE_CREATED_AT, Instant.now().toString());

        addGlobalValues(principal, model, "Fusion de comptes", null);
        model.addAttribute("user", target);
        model.addAttribute("otherUser", source);
        model.addAttribute("userTeams", teamService.getUserTeams(target));
        model.addAttribute("otherUserTeams", teamService.getUserTeams(source));
        model.addAttribute("action", "/account/merge");
        model.addAttribute("cancelUrl", "/account/complete");
        model.addAttribute("privateTeamLost", userMergeService.wouldLosePrivateTeam(source, target));

        return "account_merge";

    }

    /**
     * Fusion confirmee. Tout est reverifie : le formulaire ne porte aucune donnee de decision.
     */
    @PostMapping
    public String confirm(Principal principal,
                          HttpSession session,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final String sourceId = sessionAttribute(session, PENDING_MERGE_SOURCE).orElse(null);
        final String targetId = sessionAttribute(session, PENDING_MERGE_TARGET).orElse(null);

        if (sourceId == null || targetId == null || isConfirmationExpired(session)) {
            clearPendingMerge(session);
            attributes.addFlashAttribute("errors", List.of("Aucune fusion de comptes en attente."));
            return "redirect:/account/complete";
        }

        // la session doit toujours etre celle du compte demandeur : rien ne garantit qu'elle n'a
        // pas change entre l'affichage de l'ecran et la confirmation.
        if (!optionalConnectedUser.get().getId().equals(sourceId)) {
            clearPendingMerge(session);
            attributes.addFlashAttribute("errors", List.of("Aucune fusion de comptes en attente."));
            return "redirect:/account/complete";
        }

        final User merged;
        try {
            merged = userService.merge(sourceId, targetId);
        } catch (Exception e) {
            log.error("Unable to merge account {} into {}", sourceId, targetId, e);
            clearPendingMerge(session);
            attributes.addFlashAttribute("errors", List.of("La fusion des comptes a échoué."));
            return "redirect:/account/complete";
        }

        clearPendingMerge(session);

        // le compte de la session vient d'etre supprime : le principal doit imperativement etre
        // reconstruit sur le compte conserve, dans cette meme requete. C'est legitime parce que la
        // possession des DEUX comptes a ete prouvee (lien recu par email + session du demandeur).
        securityContextService.refreshCurrentUser(merged, request, response);
        accountCompletionService.invalidate(request);

        attributes.addFlashAttribute("infos",
                List.of("Les deux comptes ont été fusionnés. Vous êtes maintenant connecté sur votre compte conservé."));
        return "redirect:/users/me";

    }

    /**
     * Compte conserve, epingle a l'emission du token. On n'utilise l'adresse que comme repli pour
     * les tokens emis avant l'ajout de related_user_id.
     */
    private Optional<User> resolveTarget(UserAuthToken token) {
        if (token.getRelatedUserId() != null) {
            return userService.get(token.getRelatedUserId()).filter(user -> !user.isDeletion());
        }
        return userService.getByEmail(token.getTargetEmail());
    }

    private boolean isConfirmationExpired(HttpSession session) {
        return sessionAttribute(session, PENDING_MERGE_CREATED_AT)
                .map(value -> {
                    try {
                        return Instant.parse(value).plus(CONFIRMATION_VALIDITY).isBefore(Instant.now());
                    } catch (Exception e) {
                        return true;
                    }
                })
                .orElse(true);
    }

    private Optional<String> sessionAttribute(HttpSession session, String name) {
        if (session == null) {
            return Optional.empty();
        }
        final Object value = session.getAttribute(name);
        return value instanceof String stringValue ? Optional.of(stringValue) : Optional.empty();
    }

    private void clearPendingMerge(HttpSession session) {
        if (session != null) {
            session.removeAttribute(PENDING_MERGE_SOURCE);
            session.removeAttribute(PENDING_MERGE_TARGET);
            session.removeAttribute(PENDING_MERGE_CREATED_AT);
        }
    }

}
