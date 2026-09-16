package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.NoneAttestationStatement;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import info.tomacla.biketeam.domain.user.UserPasskey;
import info.tomacla.biketeam.domain.user.UserPasskeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistance des passkeys et conversion vers le modele de webauthn4j.
 */
@Service
public class PasskeyService {

    private static final int MAX_LABEL_LENGTH = 80;

    private static final String DEFAULT_LABEL = "Passkey";

    @Autowired
    private UserPasskeyRepository userPasskeyRepository;

    private final ObjectConverter objectConverter = new ObjectConverter();

    private final AttestedCredentialDataConverter attestedCredentialDataConverter =
            new AttestedCredentialDataConverter(objectConverter);

    public List<UserPasskey> listByUser(String userId) {
        return userPasskeyRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public long countByUser(String userId) {
        return userPasskeyRepository.countByUserId(userId);
    }

    public Optional<UserPasskey> getByCredentialId(String credentialId) {
        return userPasskeyRepository.findByCredentialId(credentialId);
    }

    /**
     * Suppression restreinte au proprietaire : l'identifiant de passkey vient de l'URL, il ne
     * doit jamais suffire a supprimer la passkey d'un autre compte.
     */
    @Transactional
    public boolean delete(String passkeyId, String userId) {
        return userPasskeyRepository.findByIdAndUserId(passkeyId, userId)
                .map(passkey -> {
                    userPasskeyRepository.delete(passkey);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void deleteByUser(String userId) {
        userPasskeyRepository.deleteByUserId(userId);
    }

    @Transactional
    public UserPasskey save(UserPasskey passkey) {
        return userPasskeyRepository.save(passkey);
    }

    /**
     * Enregistre le compteur de signature et la date d'utilisation apres une connexion reussie.
     */
    @Transactional
    public void recordUsage(String passkeyId, long signCount, boolean uvInitialized, boolean backedUp) {
        userPasskeyRepository.findById(passkeyId).ifPresent(passkey -> {
            passkey.setSignCount(signCount);
            passkey.setBackedUp(backedUp);
            if (uvInitialized) {
                passkey.setUvInitialized(true);
            }
            passkey.setLastUsedAt(Instant.now());
            userPasskeyRepository.save(passkey);
        });
    }

    public byte[] serializeAttestedCredentialData(AttestedCredentialData attestedCredentialData) {
        return attestedCredentialDataConverter.convert(attestedCredentialData);
    }

    /**
     * Reconstruit l'objet attendu par webauthn4j a partir de la ligne stockee.
     * <p>
     * L'attestation est volontairement remplacee par {@code none} : l'application demande
     * {@code attestation: "none"} a l'enregistrement, elle ne dispose donc d'aucun certificat a
     * restituer, et la verification d'une assertion n'en utilise pas.
     */
    public CredentialRecord toCredentialRecord(UserPasskey passkey) {
        return new CredentialRecordImpl(
                new NoneAttestationStatement(),
                passkey.isUvInitialized(),
                passkey.isBackupEligible(),
                passkey.isBackedUp(),
                passkey.getSignCount(),
                attestedCredentialDataConverter.convert(passkey.getAttestedCredentialData()),
                new AuthenticationExtensionsAuthenticatorOutputs<>(),
                null,
                new AuthenticationExtensionsClientOutputs<>(),
                toTransports(passkey.getTransports()));
    }

    public static Set<AuthenticatorTransport> toTransports(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .map(AuthenticatorTransport::create)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String fromTransports(Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return null;
        }
        return transports.stream()
                .map(AuthenticatorTransport::getValue)
                .collect(Collectors.joining(","));
    }

    /**
     * Le libelle est saisi par l'utilisateur et reaffiche tel quel dans /users/me : il est
     * borne en longueur et debarrasse des caracteres de controle. L'echappement HTML reste
     * assure par FreeMarker.
     */
    public static String sanitizeLabel(String label) {
        if (label == null) {
            return DEFAULT_LABEL;
        }
        final String cleaned = label.replaceAll("\\p{Cntrl}", " ").trim();
        if (cleaned.isEmpty()) {
            return DEFAULT_LABEL;
        }
        return cleaned.length() > MAX_LABEL_LENGTH ? cleaned.substring(0, MAX_LABEL_LENGTH) : cleaned;
    }

}
