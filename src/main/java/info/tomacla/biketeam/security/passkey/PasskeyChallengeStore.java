package info.tomacla.biketeam.security.passkey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

/**
 * Challenges WebAuthn en attente, portes par la session HTTP.
 * <p>
 * Le challenge est la seule protection contre le rejeu d'une assertion capturee : il doit etre
 * imprevisible, lie a la session qui l'a demande, et CONSOMME au premier usage. C'est pour cette
 * derniere raison que {@link #consume} supprime systematiquement l'attribut, y compris lorsque
 * la verification qui suit echoue : autrement, un attaquant pourrait retenter indefiniment une
 * assertion forgee contre le meme challenge.
 * <p>
 * La session convient pour l'authentification comme pour l'enregistrement : a la connexion la
 * session existe deja (elle est anonyme, mais le cookie est bien pose par la reponse d'options).
 */
@Service
public class PasskeyChallengeStore {

    static final String REGISTRATION_ATTR = "BIKETEAM_PASSKEY_REGISTRATION_CHALLENGE";
    static final String AUTHENTICATION_ATTR = "BIKETEAM_PASSKEY_AUTHENTICATION_CHALLENGE";

    private static final int CHALLENGE_LENGTH = 32;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private PasskeyProperties passkeyProperties;

    public PasskeyChallenge newRegistrationChallenge(HttpServletRequest request, String userId) {
        return store(request, REGISTRATION_ATTR, userId);
    }

    public PasskeyChallenge newAuthenticationChallenge(HttpServletRequest request) {
        return store(request, AUTHENTICATION_ATTR, null);
    }

    public Optional<PasskeyChallenge> consumeRegistrationChallenge(HttpServletRequest request) {
        return consume(request, REGISTRATION_ATTR);
    }

    public Optional<PasskeyChallenge> consumeAuthenticationChallenge(HttpServletRequest request) {
        return consume(request, AUTHENTICATION_ATTR);
    }

    private PasskeyChallenge store(HttpServletRequest request, String attribute, String userId) {

        final byte[] value = new byte[CHALLENGE_LENGTH];
        random.nextBytes(value);

        final PasskeyChallenge challenge = new PasskeyChallenge(value,
                Instant.now().plus(passkeyProperties.getChallengeValidity()), userId);

        request.getSession(true).setAttribute(attribute, challenge);

        return challenge;

    }

    private Optional<PasskeyChallenge> consume(HttpServletRequest request, String attribute) {

        final HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }

        final Object stored = session.getAttribute(attribute);
        session.removeAttribute(attribute);

        if (!(stored instanceof PasskeyChallenge challenge) || challenge.isExpired(Instant.now())) {
            return Optional.empty();
        }

        return Optional.of(challenge);

    }

}
