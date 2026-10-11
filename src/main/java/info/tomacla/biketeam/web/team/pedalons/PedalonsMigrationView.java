package info.tomacla.biketeam.web.team.pedalons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vue d'une demande de migration pour la page d'administration : les champs de la ligne et ce
 * qui est lisible dans le dernier statut recu de Pedalons (progression, compteurs,
 * avertissements).
 */
public class PedalonsMigrationView {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // libelles des compteurs, dans l'ordre d'affichage
    private static final Map<String, String> COUNT_LABELS = new LinkedHashMap<>();

    static {
        COUNT_LABELS.put("teamPages", "Pages du groupe");
        COUNT_LABELS.put("places", "Lieux");
        COUNT_LABELS.put("routes", "Parcours");
        COUNT_LABELS.put("rideTemplates", "Modèles de ride");
        COUNT_LABELS.put("publications", "Publications");
        COUNT_LABELS.put("rides", "Rides");
        COUNT_LABELS.put("trips", "Trips");
        COUNT_LABELS.put("tripStages", "Étapes");
        COUNT_LABELS.put("images", "Images");
    }

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "REQUESTED", "En attente de confirmation sur Pédalons",
            "CANCELLED", "Annulée",
            "EXPIRED", "Expirée",
            "TRIGGERING", "Déclenchement",
            "RUNNING", "En cours",
            "SUCCEEDED", "Terminée",
            "FAILED", "Échec");

    public static class Count {
        private final String label;
        private final long total;
        private final long migrated;
        private final long skipped;
        private final long failed;

        Count(String label, long total, long migrated, long skipped, long failed) {
            this.label = label;
            this.total = total;
            this.migrated = migrated;
            this.skipped = skipped;
            this.failed = failed;
        }

        public String getLabel() {
            return label;
        }

        public long getTotal() {
            return total;
        }

        public long getMigrated() {
            return migrated;
        }

        public long getSkipped() {
            return skipped;
        }

        public long getFailed() {
            return failed;
        }
    }

    public static class Warning {
        private final String entityType;
        private final String biketeamId;
        private final String code;
        private final String message;

        Warning(String entityType, String biketeamId, String code, String message) {
            this.entityType = entityType;
            this.biketeamId = biketeamId;
            this.code = code;
            this.message = message;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getBiketeamId() {
            return biketeamId;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private final PedalonsMigration migration;
    private final ZoneId zoneId;
    private final JsonNode result;

    public PedalonsMigrationView(PedalonsMigration migration, ZoneId zoneId, ObjectMapper objectMapper) {
        this.migration = migration;
        this.zoneId = zoneId;
        this.result = parse(migration.getResultJson(), objectMapper);
    }

    private static JsonNode parse(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return NullNode.getInstance();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return NullNode.getInstance();
        }
    }

    private String format(Instant instant) {
        return instant == null ? "" : FORMAT.format(instant.atZone(zoneId));
    }

    public String getId() {
        return migration.getId();
    }

    public String getStatus() {
        return migration.getStatus().name();
    }

    public String getStatusLabel() {
        return STATUS_LABELS.getOrDefault(getStatus(), getStatus());
    }

    public boolean isActive() {
        return migration.getStatus().isActive();
    }

    public boolean isDryRun() {
        return migration.isDryRun();
    }

    public boolean isReset() {
        return migration.isReset();
    }

    public String getCreatedAt() {
        return format(migration.getCreatedAt());
    }

    public String getTriggeredAt() {
        return format(migration.getTriggeredAt());
    }

    public String getFinishedAt() {
        return format(migration.getFinishedAt());
    }

    public String getSwitchedAt() {
        return format(migration.getSwitchedAt());
    }

    public String getErrorCode() {
        return migration.getErrorCode() == null ? "" : migration.getErrorCode();
    }

    public String getErrorMessage() {
        return migration.getErrorMessage() == null ? "" : migration.getErrorMessage();
    }

    public String getPedalonsTeamUrl() {
        if (migration.getPedalonsTeamUrl() != null) {
            return migration.getPedalonsTeamUrl();
        }
        return result.path("targetTeam").path("url").asText("");
    }

    public String getJobId() {
        return migration.getPedalonsJobId() == null ? "" : migration.getPedalonsJobId();
    }

    public String getPhase() {
        return result.path("progress").path("phase").asText("");
    }

    public long getDone() {
        return result.path("progress").path("done").asLong(0);
    }

    public long getTotal() {
        return result.path("progress").path("total").asLong(0);
    }

    public long getAttempt() {
        return result.path("attempt").asLong(0);
    }

    /**
     * Raison de l'echec de la tentative precedente, quand Pedalons refait un essai (champ
     * lastAttemptError du statut, {code, message} ou null ; absent d'un Pedalons plus ancien) :
     * message, a defaut code, ou "" si rien a afficher. Seulement pour une demande encore active et
     * un job QUEUED/RUNNING - l'echec final, lui, est dans errorCode/errorMessage.
     */
    public String getLastAttemptError() {
        if (!isActive()) {
            return "";
        }
        final String jobStatus = result.path("status").asText("");
        if (!"QUEUED".equals(jobStatus) && !"RUNNING".equals(jobStatus)) {
            return "";
        }
        final JsonNode error = result.path("lastAttemptError");
        if (!error.isObject()) {
            return "";
        }
        final String message = error.path("message").asText("").strip();
        if (!message.isEmpty()) {
            return message;
        }
        return error.path("code").asText("").strip();
    }

    /**
     * Numero de la tentative qui a echoue : en QUEUED (attente du nouvel essai), attempt compte
     * les tentatives commencees, donc la derniere ; en RUNNING, la nouvelle tentative est deja
     * commencee, c'est la precedente. 0 si inconnu.
     */
    public long getFailedAttempt() {
        final long attempt = getAttempt();
        final long failed = "RUNNING".equals(result.path("status").asText("")) ? attempt - 1 : attempt;
        return Math.max(failed, 0);
    }

    public List<Count> getCounts() {
        final JsonNode counts = result.path("counts");
        final List<Count> list = new ArrayList<>();
        if (!counts.isObject()) {
            return list;
        }
        COUNT_LABELS.forEach((key, label) -> {
            final JsonNode c = counts.path(key);
            if (c.isObject()) {
                list.add(new Count(label, c.path("total").asLong(0), c.path("migrated").asLong(0),
                        c.path("skipped").asLong(0), c.path("failed").asLong(0)));
            }
        });
        return list;
    }

    public List<Warning> getWarnings() {
        final List<Warning> list = new ArrayList<>();
        final JsonNode warnings = result.path("warnings");
        if (warnings.isArray()) {
            for (JsonNode w : warnings) {
                list.add(new Warning(w.path("entityType").asText(""), w.path("biketeamId").asText(""),
                        w.path("code").asText(""), w.path("message").asText("")));
            }
        }
        return list;
    }

    public boolean isWarningsTruncated() {
        return result.path("warningsTruncated").asBoolean(false);
    }

}
