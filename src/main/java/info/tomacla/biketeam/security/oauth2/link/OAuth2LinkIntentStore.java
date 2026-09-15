package info.tomacla.biketeam.security.oauth2.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Duration;
import java.time.Instant;

/**
 * Intention explicite de liaison d'un compte externe.
 * <p>
 * Garde-fou indispensable : sans intention posee, un clic sur "Connexion avec Google" alors qu'une
 * session est deja ouverte doit rester une connexion normale sur le compte Google (changement de
 * compte), et jamais une liaison silencieuse du compte courant.
 * <p>
 * L'intention est posee en session par {@code GET /account/link/{registrationId}} et consommee une
 * seule fois, au retour du fournisseur.
 */
@Service
public class OAuth2LinkIntentStore {

    public static final String INTENT = "OAUTH2_LINK_INTENT";
    public static final String INTENT_EXPIRES = "OAUTH2_LINK_INTENT_EXPIRES";

    private static final Duration TTL = Duration.ofMinutes(10);

    private static final Logger log = LoggerFactory.getLogger(OAuth2LinkIntentStore.class);

    public void storeIntent(String registrationId) {

        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        attributes.setAttribute(INTENT, registrationId, RequestAttributes.SCOPE_SESSION);
        attributes.setAttribute(INTENT_EXPIRES, Instant.now().plus(TTL), RequestAttributes.SCOPE_SESSION);

        log.debug("Account link intent stored for {}", registrationId);

    }

    /**
     * Vrai si une intention de liaison correspondant a ce fournisseur est en cours et non expiree.
     * L'intention est effacee dans tous les cas : elle n'est jamais rejouable.
     */
    public boolean consumeIntent(String registrationId) {

        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }

        final Object intent = attributes.getAttribute(INTENT, RequestAttributes.SCOPE_SESSION);
        final Object expires = attributes.getAttribute(INTENT_EXPIRES, RequestAttributes.SCOPE_SESSION);

        attributes.removeAttribute(INTENT, RequestAttributes.SCOPE_SESSION);
        attributes.removeAttribute(INTENT_EXPIRES, RequestAttributes.SCOPE_SESSION);

        if (!(intent instanceof String storedRegistrationId) || !storedRegistrationId.equals(registrationId)) {
            return false;
        }

        return expires instanceof Instant expiresAt && expiresAt.isAfter(Instant.now());

    }

}
