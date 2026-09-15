package info.tomacla.biketeam.domain.user;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Passkey (WebAuthn) enregistree par un utilisateur.
 * <p>
 * Aucun secret n'est stocke ici : la cle publique COSE, seule persistee, ne permet que de
 * VERIFIER une signature. La cle privee ne quitte jamais l'authentificateur.
 * <p>
 * Le format retenu pour {@code attestedCredentialData} est celui de webauthn4j
 * (AttestedCredentialDataConverter) : AAGUID + identifiant de credential + cle COSE en CBOR.
 * Il est stable d'une version a l'autre de la bibliotheque et evite d'inventer un schema maison.
 */
@Entity
@Table(name = "user_passkey")
public class UserPasskey {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Identifiant de credential en base64url. C'est la cle de recherche a la connexion :
     * le navigateur le renvoie tel quel dans la reponse d'assertion.
     */
    @Column(name = "credential_id", nullable = false, length = 500)
    private String credentialId;

    @Column(name = "attested_credential_data", nullable = false)
    private byte[] attestedCredentialData;

    /**
     * Compteur de signature de l'authentificateur. Une valeur qui n'augmente pas alors que le
     * precedent compteur etait non nul trahit un clonage : l'assertion est alors refusee par
     * webauthn4j. Les passkeys synchronisees (iCloud, Google) laissent ce compteur a zero.
     */
    @Column(name = "sign_count", nullable = false)
    private long signCount;

    @Column(name = "uv_initialized", nullable = false)
    private boolean uvInitialized;

    @Column(name = "backup_eligible", nullable = false)
    private boolean backupEligible;

    @Column(name = "backed_up", nullable = false)
    private boolean backedUp;

    /**
     * Transports annonces par l'authentificateur, separes par des virgules (usb, nfc, ble,
     * hybrid, internal). Sert a guider l'interface du navigateur, jamais a la securite.
     */
    @Column(name = "transports", length = 100)
    private String transports;

    @Column(name = "label", nullable = false, length = 80)
    private String label;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public byte[] getAttestedCredentialData() {
        return attestedCredentialData;
    }

    public void setAttestedCredentialData(byte[] attestedCredentialData) {
        this.attestedCredentialData = attestedCredentialData;
    }

    public long getSignCount() {
        return signCount;
    }

    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }

    public boolean isUvInitialized() {
        return uvInitialized;
    }

    public void setUvInitialized(boolean uvInitialized) {
        this.uvInitialized = uvInitialized;
    }

    public boolean isBackupEligible() {
        return backupEligible;
    }

    public void setBackupEligible(boolean backupEligible) {
        this.backupEligible = backupEligible;
    }

    public boolean isBackedUp() {
        return backedUp;
    }

    public void setBackedUp(boolean backedUp) {
        this.backedUp = backedUp;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    /**
     * Passkey utilisable depuis n'importe quel appareil de l'utilisateur, parce que
     * synchronisee par son trousseau. L'information est affichee dans /users/me pour que
     * l'utilisateur sache laquelle il perdrait en cassant son telephone.
     */
    @Transient
    public boolean isSyncedAcrossDevices() {
        return backupEligible && backedUp;
    }

    /**
     * Les templates formatent les dates avec {@code _date_formatter}, un DateTimeFormatter :
     * FreeMarker 2.3 ne sait pas manipuler un Instant, il faut lui presenter un temporal
     * formatable. Voir les autres entites, qui exposent directement des ZonedDateTime.
     */
    @Transient
    public ZonedDateTime getCreatedAtUtc() {
        return createdAt == null ? null : createdAt.atZone(ZoneOffset.UTC);
    }

    @Transient
    public ZonedDateTime getLastUsedAtUtc() {
        return lastUsedAt == null ? null : lastUsedAt.atZone(ZoneOffset.UTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPasskey that = (UserPasskey) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
