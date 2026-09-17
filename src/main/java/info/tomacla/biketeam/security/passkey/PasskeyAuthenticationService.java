package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import info.tomacla.biketeam.domain.user.UserPasskey;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verification d'une assertion WebAuthn presentee a la connexion.
 * <p>
 * Le parcours est "usernameless" : {@code allowCredentials} est vide, l'authentificateur
 * presente donc lui-meme la passkey decouvrable dont il dispose pour ce RP, et c'est
 * l'identifiant de credential renvoye qui designe le compte. L'utilisateur n'a aucune adresse a
 * saisir, et le serveur ne divulgue rien sur l'existence d'un compte.
 */
@Service
public class PasskeyAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(PasskeyAuthenticationService.class);

    static final String MSG_FAILED = "Cette passkey n'a pas permis de vous identifier.";

    @Autowired
    private PasskeyProperties passkeyProperties;

    @Autowired
    private PasskeyChallengeStore challengeStore;

    @Autowired
    private PasskeyService passkeyService;

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();

    /**
     * Options pour {@code navigator.credentials.get()}.
     */
    public Map<String, Object> createOptions(HttpServletRequest request) {

        final PasskeyChallenge challenge = challengeStore.newAuthenticationChallenge(request);

        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64Url.encode(challenge.getValue()));
        options.put("rpId", passkeyProperties.getRpId());
        options.put("timeout", passkeyProperties.getChallengeValidity().toMillis());
        // liste vide, et non absente : c'est ce qui declenche le mode "credential decouvrable"
        options.put("allowCredentials", java.util.List.of());
        options.put("userVerification", "preferred");

        return options;

    }

    /**
     * @return la passkey ayant signe l'assertion, apres mise a jour de son compteur
     * @throws PasskeyException si l'assertion est invalide, expiree ou inconnue
     */
    public UserPasskey verify(HttpServletRequest request, PasskeyAuthenticationBody body) {

        final PasskeyChallenge challenge = challengeStore.consumeAuthenticationChallenge(request)
                .orElseThrow(() -> new PasskeyException("La demande de connexion a expiré, veuillez recommencer."));

        final byte[] credentialId = Base64Url.decodeOrNull(body.credentialId());
        final byte[] authenticatorData = Base64Url.decodeOrNull(body.authenticatorData());
        final byte[] clientDataJSON = Base64Url.decodeOrNull(body.clientDataJSON());
        final byte[] signature = Base64Url.decodeOrNull(body.signature());
        final byte[] userHandle = Base64Url.decodeOrNull(body.userHandle());

        if (credentialId == null || authenticatorData == null || clientDataJSON == null || signature == null) {
            throw new PasskeyException(MSG_FAILED);
        }

        final UserPasskey passkey = passkeyService.getByCredentialId(Base64Url.encode(credentialId))
                .orElseThrow(() -> new PasskeyException(MSG_FAILED));

        // webauthn4j ne rapproche pas le userHandle du compte : il ne connait pas notre modele.
        // Le controle est donc fait ici. Un authentificateur peut legitimement ne pas renvoyer de
        // userHandle (assertion non decouvrable), auquel cas il n'y a rien a comparer.
        if (userHandle != null && userHandle.length > 0) {
            final String handle = new String(userHandle, StandardCharsets.UTF_8);
            if (!passkey.getUserId().equals(handle)) {
                log.warn("Assertion passkey avec un userHandle incoherent pour la passkey {}", passkey.getId());
                throw new PasskeyException(MSG_FAILED);
            }
        }

        final ServerProperty serverProperty = new ServerProperty(
                passkeyProperties.getOrigins(),
                passkeyProperties.getRpId(),
                new DefaultChallenge(challenge.getValue()));

        final CredentialRecord credentialRecord = passkeyService.toCredentialRecord(passkey);

        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                credentialId,
                userHandle,
                authenticatorData,
                clientDataJSON,
                body.clientExtensionResults(),
                signature);

        // allowCredentials null : le parcours est decouvrable, la restriction est deja faite par
        // la recherche en base ci-dessus. userVerificationRequired reste false ("preferred").
        final AuthenticationParameters parameters = new AuthenticationParameters(
                serverProperty, credentialRecord, null, false, true);

        final AuthenticationData data;
        try {
            data = webAuthnManager.verify(authenticationRequest, parameters);
        } catch (RuntimeException e) {
            // couvre notamment la regression du compteur de signature, signe d'un clonage
            log.info("Échec de vérification d'une assertion passkey", e);
            throw new PasskeyException(MSG_FAILED);
        }

        passkeyService.recordUsage(passkey.getId(),
                data.getAuthenticatorData().getSignCount(),
                data.getAuthenticatorData().isFlagUV(),
                data.getAuthenticatorData().isFlagBS());

        return passkey;

    }

}
