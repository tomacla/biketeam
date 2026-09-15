package info.tomacla.biketeam.domain.user;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;

/**
 * Token a usage unique (email de verification ou de reinitialisation de mot de passe).
 * Le token clair n'est jamais persiste : seul son empreinte SHA-256 (tokenHash) l'est.
 */
@Entity
@Table(name = "user_auth_token")
public class UserAuthToken {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private UserAuthTokenType type;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "target_email", length = 150)
    private String targetEmail;

    /**
     * Second compte implique, pour les tokens {@link UserAuthTokenType#ACCOUNT_MERGE} : le
     * compte destinataire du lien, celui qui sera conserve par la fusion.
     * <p>
     * Epingler son identifiant plutot que de le reresoudre par {@code targetEmail} au moment de
     * la consommation est une precaution de securite : entre l'emission et le clic, l'adresse
     * peut avoir change de proprietaire, et la fusion se ferait alors vers un compte tiers.
     * Null pour tous les autres types.
     */
    @Column(name = "related_user_id")
    private String relatedUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(String relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserAuthTokenType getType() {
        return type;
    }

    public void setType(UserAuthTokenType type) {
        this.type = type;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthToken that = (UserAuthToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
