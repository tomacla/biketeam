package info.tomacla.biketeam.domain.pedalons;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Une demande de migration d'une equipe vers Pedalons, et son suivi.
 * <p>
 * L'identifiant est le requestId (UUID v4 en minuscules) : c'est aussi le claim jti du jeton de
 * demande signe, et la cle que Pedalons renvoie dans le callback. La ligne n'est jamais
 * supprimee (historique affiche sur la page d'administration).
 */
@Entity
@Table(name = "pedalons_migration")
public class PedalonsMigration {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    // simple colonne et non association : le compte peut disparaitre (ON DELETE SET NULL)
    @Column(name = "requested_by")
    private String requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PedalonsMigrationStatus status;

    @Column(name = "dry_run", nullable = false)
    private boolean dryRun;

    @Column(nullable = false)
    private boolean reset;

    // grant a usage unique recu de Pedalons : garde le temps du declenchement, efface ensuite
    @Column(name = "grant_value", length = 100)
    private String grantValue;

    @Column(name = "pedalons_job_id", length = 40)
    private String pedalonsJobId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    // premier statut RUNNING rapporte par Pedalons : le delai d'execution part de la, pas de
    // triggeredAt (le job peut attendre longtemps en QUEUED derriere celui d'une autre equipe)
    @Column(name = "running_since")
    private Instant runningSince;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "switched_at")
    private Instant switchedAt;

    @Column(name = "error_code", length = 60)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    // dernier statut recu de Pedalons, tel quel
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "pedalons_team_url", length = 1000)
    private String pedalonsTeamUrl;

    public PedalonsMigration() {
    }

    public PedalonsMigration(String id, String teamId, String requestedBy, boolean dryRun, boolean reset, Instant now) {
        this.id = Objects.requireNonNull(id, "id is null");
        this.teamId = Objects.requireNonNull(teamId, "teamId is null");
        this.requestedBy = requestedBy;
        this.dryRun = dryRun;
        this.reset = reset;
        this.status = PedalonsMigrationStatus.REQUESTED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public PedalonsMigrationStatus getStatus() {
        return status;
    }

    public void setStatus(PedalonsMigrationStatus status, Instant now) {
        this.status = Objects.requireNonNull(status, "status is null");
        this.updatedAt = now;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isReset() {
        return reset;
    }

    public String getGrantValue() {
        return grantValue;
    }

    public void setGrantValue(String grantValue) {
        this.grantValue = grantValue;
    }

    public String getPedalonsJobId() {
        return pedalonsJobId;
    }

    public void setPedalonsJobId(String pedalonsJobId) {
        this.pedalonsJobId = pedalonsJobId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getRunningSince() {
        return runningSince;
    }

    public void setRunningSince(Instant runningSince) {
        this.runningSince = runningSince;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Instant getSwitchedAt() {
        return switchedAt;
    }

    public void setSwitchedAt(Instant switchedAt) {
        this.switchedAt = switchedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setError(String errorCode, String errorMessage) {
        this.errorCode = truncate(errorCode, 60);
        this.errorMessage = truncate(errorMessage, 2000);
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getPedalonsTeamUrl() {
        return pedalonsTeamUrl;
    }

    public void setPedalonsTeamUrl(String pedalonsTeamUrl) {
        this.pedalonsTeamUrl = truncate(pedalonsTeamUrl, 1000);
    }

    public static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PedalonsMigration that = (PedalonsMigration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
