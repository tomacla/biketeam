package info.tomacla.biketeam.security.passkey;

import java.io.Serializable;
import java.time.Instant;

/**
 * Challenge WebAuthn en attente, conserve dans la session HTTP.
 * <p>
 * Serialisable : les sessions sont persistees en base (spring.session.store-type=jdbc).
 */
public class PasskeyChallenge implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] value;
    private final Instant expiresAt;

    /**
     * Identifiant de l'utilisateur pour lequel le challenge a ete emis (enregistrement
     * uniquement). Verifie a la finalisation : sans lui, un challenge obtenu sur un compte
     * puis rejoue apres changement de session poserait la passkey sur le mauvais compte.
     */
    private final String userId;

    public PasskeyChallenge(byte[] value, Instant expiresAt, String userId) {
        this.value = value;
        this.expiresAt = expiresAt;
        this.userId = userId;
    }

    public byte[] getValue() {
        return value;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

}
