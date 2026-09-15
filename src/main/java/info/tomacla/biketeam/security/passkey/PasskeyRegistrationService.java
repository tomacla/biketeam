package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserPasskey;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enregistrement d'une passkey sur un compte DEJA CONNECTE.
 * <p>
 * C'est la regle produit qui structure tout ce service : on ne cree jamais de compte par passkey.
 * L'utilisateur s'authentifie d'abord par email + mot de passe ou par Google/Facebook, puis
 * declare une passkey depuis /users/me. La consequence est qu'aucun parcours de recuperation
 * specifique n'est necessaire : perdre toutes ses passkeys ramene simplement a la connexion
 * d'origine, qui reste en place.
 */
@Service
public class PasskeyRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(PasskeyRegistrationService.class);

    /**
     * Algorithmes annonces au navigateur, par ordre de preference. ES256 est universellement
     * supporte ; RS256 reste necessaire pour Windows Hello ; EdDSA couvre les cles recentes.
     */
    private static final List<PublicKeyCredentialParameters> CREDENTIAL_PARAMETERS = List.of(
            new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
            new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256),
            new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.EdDSA));

    @Autowired
    private PasskeyProperties passkeyProperties;

    @Autowired
    private PasskeyChallengeStore challengeStore;

    @Autowired
    private PasskeyService passkeyService;

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();

    /**
     * Options de creation, au format attendu par {@code navigator.credentials.create()}.
     * <p>
     * {@code residentKey: required} est indispensable : sans credential decouvrable, le
     * navigateur ne peut pas proposer la passkey a un utilisateur qui n'a pas encore saisi son
     * identifiant, et la promesse "se connecter d'un geste" tombe.
     * <p>
     * {@code attestation: none} est un choix assume : l'attestation identifierait le modele exact de
     * l'authentificateur sans rien apporter a la securite d'un site grand public, et impose de
     * gerer des chaines de certification.
     */
    public Map<String, Object> createOptions(HttpServletRequest request, User user) {

        if (passkeyService.countByUser(user.getId()) >= passkeyProperties.getMaxPerUser()) {
            throw new PasskeyException("Vous avez atteint le nombre maximum de passkeys enregistrées.");
        }

        final PasskeyChallenge challenge = challengeStore.newRegistrationChallenge(request, user.getId());

        final Map<String, Object> rp = new LinkedHashMap<>();
        rp.put("id", passkeyProperties.getRpId());
        rp.put("name", passkeyProperties.getRpName());

        // l'identifiant utilisateur WebAuthn est un OPAQUE renvoye tel quel par l'authentificateur
        // a la connexion : on y met l'id technique, jamais l'email (il change, et il serait
        // stocke en clair dans le trousseau de l'appareil)
        final Map<String, Object> userEntity = new LinkedHashMap<>();
        userEntity.put("id", Base64Url.encode(user.getId().getBytes(StandardCharsets.UTF_8)));
        userEntity.put("name", user.getEmail() != null ? user.getEmail() : user.getIdentity());
        userEntity.put("displayName", user.getIdentity());

        final List<Map<String, Object>> excludeCredentials = new ArrayList<>();
        passkeyService.listByUser(user.getId()).forEach(existing -> {
            final Map<String, Object> descriptor = new LinkedHashMap<>();
            descriptor.put("type", "public-key");
            descriptor.put("id", existing.getCredentialId());
            final Set<AuthenticatorTransport> transports = PasskeyService.toTransports(existing.getTransports());
            if (!transports.isEmpty()) {
                descriptor.put("transports", transports.stream().map(AuthenticatorTransport::getValue).toList());
            }
            excludeCredentials.add(descriptor);
        });

        final Map<String, Object> authenticatorSelection = new LinkedHashMap<>();
        authenticatorSelection.put("residentKey", "required");
        authenticatorSelection.put("requireResidentKey", true);
        authenticatorSelection.put("userVerification", "preferred");

        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("rp", rp);
        options.put("user", userEntity);
        options.put("challenge", Base64Url.encode(challenge.getValue()));
        options.put("pubKeyCredParams", CREDENTIAL_PARAMETERS.stream()
                .map(p -> Map.<String, Object>of("type", p.getType().getValue(), "alg", p.getAlg().getValue()))
                .toList());
        options.put("timeout", passkeyProperties.getChallengeValidity().toMillis());
        options.put("excludeCredentials", excludeCredentials);
        options.put("authenticatorSelection", authenticatorSelection);
        options.put("attestation", "none");

        return options;

    }

    /**
     * Verifie l'attestation renvoyee par le navigateur et persiste la passkey.
     */
    public UserPasskey finish(HttpServletRequest request, User user, PasskeyRegistrationBody body) {

        final PasskeyChallenge challenge = challengeStore.consumeRegistrationChallenge(request)
                .orElseThrow(() -> new PasskeyException("La demande a expiré, veuillez recommencer."));

        // le challenge est lie au compte qui l'a demande : sans ce controle, un challenge obtenu
        // sur un compte puis reutilise apres bascule de session poserait la passkey ailleurs
        if (!user.getId().equals(challenge.getUserId())) {
            throw new PasskeyException("La demande ne correspond pas à votre compte, veuillez recommencer.");
        }

        final byte[] attestationObject = Base64Url.decodeOrNull(body.attestationObject());
        final byte[] clientDataJSON = Base64Url.decodeOrNull(body.clientDataJSON());

        if (attestationObject == null || clientDataJSON == null) {
            throw new PasskeyException("Réponse de l'appareil invalide.");
        }

        final ServerProperty serverProperty = new ServerProperty(
                passkeyProperties.getOrigins(),
                passkeyProperties.getRpId(),
                new DefaultChallenge(challenge.getValue()));

        final RegistrationRequest registrationRequest = new RegistrationRequest(
                attestationObject,
                clientDataJSON,
                body.clientExtensionResults(),
                body.transports() == null ? Set.of() : Set.copyOf(body.transports()));

        // userVerificationRequired = false : la verification d'utilisateur est demandee
        // ("preferred") mais non exigee, pour ne pas exclure les cles de securite sans code PIN.
        final RegistrationParameters parameters = new RegistrationParameters(
                serverProperty, CREDENTIAL_PARAMETERS, false, true);

        final RegistrationData data;
        try {
            data = webAuthnManager.verify(registrationRequest, parameters);
        } catch (RuntimeException e) {
            log.info("Échec de vérification d'un enregistrement de passkey pour {}", user.getId(), e);
            throw new PasskeyException("Cette passkey n'a pas pu être vérifiée.");
        }

        final AuthenticatorData<?> authenticatorData = data.getAttestationObject().getAuthenticatorData();
        final AttestedCredentialData attestedCredentialData = authenticatorData.getAttestedCredentialData();

        if (attestedCredentialData == null) {
            throw new PasskeyException("Cette passkey n'a pas pu être vérifiée.");
        }

        final String credentialId = Base64Url.encode(attestedCredentialData.getCredentialId());

        // excludeCredentials devrait deja l'avoir empeche, mais rien n'oblige un authentificateur
        // a l'honorer : le doublon est donc verifie cote serveur
        if (passkeyService.getByCredentialId(credentialId).isPresent()) {
            throw new PasskeyException("Cette passkey est déjà enregistrée.");
        }

        final UserPasskey passkey = new UserPasskey();
        passkey.setUserId(user.getId());
        passkey.setCredentialId(credentialId);
        passkey.setAttestedCredentialData(passkeyService.serializeAttestedCredentialData(attestedCredentialData));
        passkey.setSignCount(authenticatorData.getSignCount());
        passkey.setUvInitialized(authenticatorData.isFlagUV());
        passkey.setBackupEligible(authenticatorData.isFlagBE());
        passkey.setBackedUp(authenticatorData.isFlagBS());
        passkey.setTransports(PasskeyService.fromTransports(data.getTransports()));
        passkey.setLabel(PasskeyService.sanitizeLabel(body.label()));
        passkey.setCreatedAt(Instant.now());

        try {
            return passkeyService.save(passkey);
        } catch (DataIntegrityViolationException e) {
            // course entre deux enregistrements simultanes du meme credential
            throw new PasskeyException("Cette passkey est déjà enregistrée.");
        }

    }

}
