package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.oauth2.link.AccountLinkService;
import info.tomacla.biketeam.security.oauth2.link.OAuth2LinkIntentStore;
import info.tomacla.biketeam.security.session.SecurityContextService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Liaison d'un compte externe a la session courante, et resolution des conflits d'identite.
 * <p>
 * Le parcours passe obligatoirement par {@code /account/link/{registrationId}} : c'est le seul
 * endroit qui pose l'intention de liaison. Un acces direct a /oauth2/authorization/... reste une
 * connexion normale (changement de compte).
 */
@Controller
@RequestMapping(value = "/account")
public class AccountLinkController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AccountLinkController.class);

    /**
     * Strava n'est volontairement pas liable : c'est une connexion de transition, destinee a
     * disparaitre.
     */
    private static final Set<String> LINKABLE_PROVIDERS = Set.of("google", "facebook");

    @Autowired
    private OAuth2LinkIntentStore linkIntentStore;

    @Autowired
    private AccountLinkService accountLinkService;

    @Autowired
    private SecurityContextService securityContextService;


    @Autowired
    private UserMergeService userMergeService;

    @GetMapping(value = "/link/{registrationId}")
    public String link(@PathVariable("registrationId") String registrationId,
                       Principal principal,
                       RedirectAttributes attributes) {

        if (getUserFromPrincipal(principal).isEmpty()) {
            return "redirect:/";
        }

        if (!LINKABLE_PROVIDERS.contains(registrationId)) {
            attributes.addFlashAttribute("errors", List.of("Ce fournisseur ne peut pas être lié à votre compte."));
            return "redirect:/users/me";
        }

        linkIntentStore.storeIntent(registrationId);

        return "redirect:/oauth2/authorization/" + registrationId;

    }

    @GetMapping(value = "/link-conflict")
    public String linkConflict(Principal principal,
                               Model model,
                               HttpSession session,
                               RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final Optional<User> optionalOtherUser = accountLinkService.getPendingMergeUserId(session)
                .flatMap(userService::get);

        if (optionalOtherUser.isEmpty()) {
            attributes.addFlashAttribute("errors", List.of("Aucune fusion de comptes en attente."));
            return "redirect:/users/me";
        }

        final User user = optionalConnectedUser.get();
        final User otherUser = optionalOtherUser.get();

        addGlobalValues(principal, model, "Fusion de comptes", null);
        model.addAttribute("user", user);
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("provider", accountLinkService.getPendingMergeProvider(session).orElse(""));
        model.addAttribute("userTeams", teamService.getUserTeams(user));
        model.addAttribute("otherUserTeams", teamService.getUserTeams(otherUser));
        model.addAttribute("privateTeamLost", userMergeService.wouldLosePrivateTeam(otherUser, user));

        return "account_merge";

    }

    /**
     * Fusion confirmee. Le sens est impose : la cible est TOUJOURS le compte de la session
     * courante (le cookie remember-me, la session et les autorites portent son identifiant).
     */
    @PostMapping(value = "/link-conflict")
    public String mergeConflict(Principal principal,
                                HttpSession session,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                RedirectAttributes attributes) {

        final Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final Optional<User> optionalOtherUser = accountLinkService.getPendingMergeUserId(session)
                .flatMap(userService::get);

        if (optionalOtherUser.isEmpty()) {
            attributes.addFlashAttribute("errors", List.of("Aucune fusion de comptes en attente."));
            return "redirect:/users/me";
        }

        final User target = optionalConnectedUser.get();
        final User source = optionalOtherUser.get();

        if (source.getId().equals(target.getId())) {
            accountLinkService.clearPendingMerge(session);
            return "redirect:/users/me";
        }

        // les identites portees par le compte source sont relevees avant la fusion : merge les
        // deplace, mais seulement si la cible n'en porte pas deja
        final String sourceGoogleId = source.getGoogleId();
        final String sourceFacebookId = source.getFacebookId();

        // le conflit peut aussi avoir ete declenche par l'ADRESSE : l'autre compte ne porte alors
        // aucune identite de ce fournisseur, et l'authentification OAuth2 s'est interrompue avant
        // que le sub/id ne soit persiste. Le sujet prouve lors de cette tentative est repris ici,
        // sinon la fusion aboutirait sans realiser la liaison demandee.
        final String pendingProvider = accountLinkService.getPendingMergeProvider(session).orElse("");
        final String pendingSubject = accountLinkService.getPendingMergeSubject(session).orElse(null);

        User merged;
        try {
            // merge relit et renvoie le compte conserve : les requetes natives de la fusion
            // detachent tout le contexte de persistance, `target` n'est plus fiable ici.
            merged = userService.merge(source.getId(), target.getId());
        } catch (Exception e) {
            log.error("Unable to merge accounts", e);
            attributes.addFlashAttribute("errors", List.of("La fusion des comptes a échoué."));
            return "redirect:/users/me";
        }

        final String googleId = sourceGoogleId != null ? sourceGoogleId
                : ("google".equals(pendingProvider) ? pendingSubject : null);
        final String facebookId = sourceFacebookId != null ? sourceFacebookId
                : ("facebook".equals(pendingProvider) ? pendingSubject : null);

        boolean updated = false;
        if (googleId != null && merged.getGoogleId() == null) {
            merged.setGoogleId(googleId);
            updated = true;
        }
        if (facebookId != null && merged.getFacebookId() == null) {
            merged.setFacebookId(facebookId);
            updated = true;
        }
        if (updated) {
            merged = userService.save(merged);
        }

        // le compte source vient d'etre supprime : le principal doit imperativement etre
        // reconstruit sur la cible, et le contexte sauvegarde, dans cette meme requete
        securityContextService.refreshCurrentUser(merged, request, response);

        // le compte fusionne porte desormais une identite externe : le cache de completion
        // de la session doit etre relu
        accountCompletionService.invalidate(request);

        accountLinkService.clearPendingMerge(session);

        attributes.addFlashAttribute("infos", List.of("Les deux comptes ont été fusionnés."));
        return "redirect:/users/me";

    }

}
