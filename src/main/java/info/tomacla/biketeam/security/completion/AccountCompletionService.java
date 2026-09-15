package info.tomacla.biketeam.security.completion;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.mail.MailSenderService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Etat de completion du compte de la session courante.
 * <p>
 * Un compte est incomplet lorsqu'il ne dispose ni d'un couple (email verifie + mot de passe) ni
 * d'une identite Google ou Facebook : typiquement un compte cree par la connexion Strava, qui est
 * une connexion de transition destinee a disparaitre.
 * <p>
 * Le resultat est mis en cache dans la session HTTP : la lecture en base n'a lieu que lorsque le
 * principal ne porte pas l'information (session serialisee avant le deploiement de cette version).
 */
@Service
public class AccountCompletionService {

    private static final Logger log = LoggerFactory.getLogger(AccountCompletionService.class);

    public static final String SESSION_ATTR = "BIKETEAM_ACCOUNT_COMPLETE";

    /**
     * Attribut porte par le principal. Une valeur absente signifie "inconnu", jamais "complet".
     */
    private static final String PRINCIPAL_ATTR = "accountComplete";

    @Autowired
    private UserService userService;

    @Autowired
    private MailSenderService mailSenderService;

    @Value("${auth.completion.enabled:true}")
    private boolean completionEnabled;

    @PostConstruct
    public void init() {
        if (completionEnabled && !mailSenderService.isSmtpConfigured()) {
            log.warn("Complétion de compte désactivée : SMTP non configuré.");
        }
    }

    /**
     * Le forcage n'a de sens que si l'utilisateur peut effectivement verifier une adresse : sans
     * SMTP, il serait enferme dans la boucle de redirection. Evalue a CHAQUE requete pour que
     * la propriete serve de retour arriere immediat.
     */
    public boolean enforcementEnabled() {
        return completionEnabled && mailSenderService.isSmtpConfigured();
    }

    /**
     * Cascade : cache de session positif, attribut du principal, cache de session negatif, base.
     * <p>
     * Le TRUE de session passe AVANT le principal, et c'est indispensable : le principal porte
     * l'etat fige au moment de la connexion. Si le compte est complete depuis un autre navigateur
     * (lien de verification ouvert sur le telephone, par exemple), la session restee ouverte porte
     * un principal perime disant "incomplet" alors que la base dit "complet". Le filtre renverrait
     * alors vers /account/complete, dont le controleur - voyant le compte complet en base -
     * redirigerait vers "/", et ainsi de suite : boucle de redirection infinie. Le TRUE pose par
     * {@link #markComplete(HttpServletRequest)} est la sortie de secours de cette boucle.
     */
    public boolean isComplete(Authentication authentication, HttpServletRequest request) {

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2UserDetails userDetails)) {
            // rien a forcer : ce n'est pas un utilisateur de l'application
            return true;
        }

        final HttpSession existingSession = request == null ? null : request.getSession(false);
        final Object cached = existingSession == null ? null : existingSession.getAttribute(SESSION_ATTR);

        // 1. cache de session positif : jamais perime (il n'est pose qu'apres constat de completude)
        if (Boolean.TRUE.equals(cached)) {
            return true;
        }

        // 2. attribut du principal
        final Boolean fromPrincipal = userDetails.getAttribute(PRINCIPAL_ATTR);
        if (fromPrincipal != null) {
            return fromPrincipal;
        }

        // 3. cache de session negatif (principal issu d'une session serialisee avant deploiement)
        if (cached instanceof Boolean cachedValue) {
            return cachedValue;
        }

        // 4. relecture en base
        final boolean complete = userService.get(userDetails.getUsername())
                .map(User::isAccountComplete)
                .orElse(false);

        if (request != null) {
            request.getSession(true).setAttribute(SESSION_ATTR, complete);
        }

        return complete;

    }

    public void markComplete(HttpServletRequest request) {
        if (request != null) {
            request.getSession(true).setAttribute(SESSION_ATTR, Boolean.TRUE);
        }
    }

    public void invalidate(HttpServletRequest request) {
        final HttpSession session = request == null ? null : request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_ATTR);
        }
    }

}
