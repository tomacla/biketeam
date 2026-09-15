package info.tomacla.biketeam.security.oauth2.link;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.CurrentUserResolver;
import info.tomacla.biketeam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/**
 * Liaison d'une identite OAuth2 a un compte deja authentifie.
 * <p>
 * Table de decision (appliquee uniquement par les handlers Google et Facebook, jamais par Strava) :
 * <table>
 * <tr><td>pas d'authentification</td><td>identite inconnue</td><td>creation de compte</td></tr>
 * <tr><td>pas d'authentification</td><td>identite connue</td><td>connexion</td></tr>
 * <tr><td>authentifie, SANS intention</td><td>quelconque</td><td>connexion normale sur le compte OAuth (changement de compte)</td></tr>
 * <tr><td>authentifie + intention</td><td>identite inconnue, email libre</td><td>liaison sur le compte courant</td></tr>
 * <tr><td>authentifie + intention</td><td>identite connue, meme compte</td><td>rafraichissement seul</td></tr>
 * <tr><td>authentifie + intention</td><td>identite ou email d'un autre compte</td><td>conflit : ecran de fusion</td></tr>
 * </table>
 * <p>
 * Aucune fusion n'est jamais automatique : {@link UserService#merge} perd les participations aux
 * sorties, elle ne peut se faire que sur confirmation explicite de l'utilisateur.
 */
@Service
public class AccountLinkService {

    public static final String ACCOUNT_LINK_CONFLICT = "account_link_conflict";
    public static final String PENDING_ACCOUNT_MERGE = "PENDING_ACCOUNT_MERGE";
    public static final String PENDING_ACCOUNT_MERGE_PROVIDER = "PENDING_ACCOUNT_MERGE_PROVIDER";

    /**
     * Sujet OAuth2 (sub Google / id Facebook) prouve par l'authentification qui vient d'echouer
     * sur un conflit. Il est conserve pour que la fusion confirmee puisse poser l'identite sur le
     * compte cible : sans lui, le conflit declenche par l'ADRESSE (l'autre compte ne porte alors
     * aucune identite de ce fournisseur) se terminerait par une fusion reussie mais sans aucune
     * liaison, c'est-a-dire sans faire ce que l'utilisateur avait demande.
     */
    public static final String PENDING_ACCOUNT_MERGE_SUBJECT = "PENDING_ACCOUNT_MERGE_SUBJECT";

    private static final Logger log = LoggerFactory.getLogger(AccountLinkService.class);

    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;
    private final OAuth2LinkIntentStore linkIntentStore;

    @Autowired
    public AccountLinkService(UserService userService,
                              CurrentUserResolver currentUserResolver,
                              OAuth2LinkIntentStore linkIntentStore) {
        this.userService = userService;
        this.currentUserResolver = currentUserResolver;
        this.linkIntentStore = linkIntentStore;
    }

    /**
     * Compte de la session courante sur lequel l'identite doit etre liee.
     *
     * @param registrationId  google ou facebook
     * @param providerSubject identifiant de l'utilisateur chez le fournisseur (sub / id)
     * @param identityOwner   compte portant deja cette identite OAuth2, s'il existe
     * @param providerEmail   email fourni par le fournisseur (peut etre null)
     * @param emailProven     vrai si le fournisseur garantit l'adresse
     * @return le compte courant a lier, ou {@link Optional#empty()} pour le comportement de
     * connexion normal (creation ou connexion sur le compte OAuth)
     * @throws OAuth2AuthenticationException code {@link #ACCOUNT_LINK_CONFLICT} si l'identite ou
     *                                       l'adresse appartient deja a un autre compte
     */
    public Optional<User> resolveLinkTarget(String registrationId,
                                            String providerSubject,
                                            Optional<User> identityOwner,
                                            String providerEmail,
                                            boolean emailProven) {

        final Optional<String> currentUserId = currentUserResolver.currentUserId();
        if (currentUserId.isEmpty()) {
            // aucune session : comportement historique (creation ou connexion)
            return Optional.empty();
        }

        // LIGNE CRITIQUE : sans intention explicite, on ne lie jamais. L'utilisateur qui clique sur
        // "Connexion avec Google" en etant deja connecte veut changer de compte, pas fusionner.
        if (!linkIntentStore.consumeIntent(registrationId)) {
            return Optional.empty();
        }

        final Optional<User> optionalCurrentUser = userService.get(currentUserId.get());
        if (optionalCurrentUser.isEmpty()) {
            return Optional.empty();
        }

        final User currentUser = optionalCurrentUser.get();

        if (identityOwner.isPresent()) {

            if (identityOwner.get().getId().equals(currentUser.getId())) {
                // identite deja liee a ce compte : simple rafraichissement
                return Optional.of(currentUser);
            }

            throw conflict(registrationId, providerSubject, identityOwner.get());

        }

        if (emailProven && !Strings.isBlank(providerEmail)) {
            final Optional<User> emailOwner = userService.getByEmail(providerEmail);
            if (emailOwner.isPresent() && !emailOwner.get().getId().equals(currentUser.getId())) {
                throw conflict(registrationId, providerSubject, emailOwner.get());
            }
        }

        return Optional.of(currentUser);

    }

    /**
     * Applique l'email fourni par le fournisseur sans jamais voler l'adresse d'un tiers.
     * <p>
     * L'index unique fonctionnel sur lower(email) rendrait l'ecriture fatale
     * (DataIntegrityViolationException) : la disponibilite est donc verifiee au prealable, et
     * l'identite OAuth2 est liee sans reprendre l'adresse si celle-ci appartient a un autre compte.
     */
    public void applyProviderEmail(User user, String email, boolean emailProven) {

        if (Strings.isBlank(email)) {
            return;
        }

        if (user.isEmailVerified() && !emailProven) {
            // une adresse deja PROUVEE ne doit jamais etre remplacee par une adresse que le
            // fournisseur ne garantit pas : setEmail remettrait emailVerified a false et
            // supprimerait silencieusement la connexion par mot de passe de l'utilisateur.
            log.warn("Identity provider returned an unproven email : verified address of user {} left untouched",
                    user.getId());
            return;
        }

        if (!userService.isEmailAvailable(email, user.getId())) {
            log.warn("Email provided by identity provider is already used by another account : "
                    + "identity linked to user {} without taking the address over", user.getId());
            return;
        }

        if (emailProven) {
            user.setVerifiedEmail(email);
        } else {
            // le fournisseur ne garantit pas l'adresse : elle devra passer par notre propre
            // lien de verification
            user.setEmail(email);
        }

    }

    private OAuth2AuthenticationException conflict(String registrationId, String providerSubject, User otherUser) {

        log.info("Account link conflict on {} : identity belongs to user {}", registrationId, otherUser.getId());

        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(PENDING_ACCOUNT_MERGE, otherUser.getId(), RequestAttributes.SCOPE_SESSION);
            attributes.setAttribute(PENDING_ACCOUNT_MERGE_PROVIDER, registrationId, RequestAttributes.SCOPE_SESSION);
            if (providerSubject != null) {
                attributes.setAttribute(PENDING_ACCOUNT_MERGE_SUBJECT, providerSubject, RequestAttributes.SCOPE_SESSION);
            }
        }

        return new OAuth2AuthenticationException(
                new OAuth2Error(ACCOUNT_LINK_CONFLICT,
                        "Un autre compte utilise deja cette identite " + registrationId, null),
                "Un autre compte utilise deja cette identite " + registrationId);

    }

    public Optional<String> getPendingMergeUserId(HttpSession session) {
        return getSessionAttribute(session, PENDING_ACCOUNT_MERGE);
    }

    public Optional<String> getPendingMergeProvider(HttpSession session) {
        return getSessionAttribute(session, PENDING_ACCOUNT_MERGE_PROVIDER);
    }

    /**
     * Sujet OAuth2 prouve lors de la tentative de liaison ayant provoque le conflit.
     */
    public Optional<String> getPendingMergeSubject(HttpSession session) {
        return getSessionAttribute(session, PENDING_ACCOUNT_MERGE_SUBJECT);
    }

    public void clearPendingMerge(HttpSession session) {
        if (session != null) {
            session.removeAttribute(PENDING_ACCOUNT_MERGE);
            session.removeAttribute(PENDING_ACCOUNT_MERGE_PROVIDER);
            session.removeAttribute(PENDING_ACCOUNT_MERGE_SUBJECT);
        }
    }

    private Optional<String> getSessionAttribute(HttpSession session, String name) {
        if (session == null) {
            return Optional.empty();
        }
        final Object value = session.getAttribute(name);
        return value instanceof String stringValue ? Optional.of(stringValue) : Optional.empty();
    }

}
