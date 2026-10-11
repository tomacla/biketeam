package info.tomacla.biketeam.domain.pedalons;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Une ligne de la table de correspondance d'URL renvoyee par Pedalons a la fin d'une migration
 * definitive : (equipe, type, id biketeam) vers l'URL absolue Pedalons.
 * <p>
 * entityType reprend les cles de urlMap : TEAM, TEAM_ABOUT, TEAM_FAQ, ROUTES_LIST, ROUTE, RIDE,
 * TRIP, TRIP_STAGE, POST.
 */
@Entity
@Table(name = "pedalons_redirect")
@IdClass(PedalonsRedirect.Key.class)
public class PedalonsRedirect {

    public static final String TEAM = "TEAM";
    public static final String TEAM_ABOUT = "TEAM_ABOUT";
    public static final String TEAM_FAQ = "TEAM_FAQ";
    public static final String ROUTES_LIST = "ROUTES_LIST";
    public static final String ROUTE = "ROUTE";
    public static final String RIDE = "RIDE";
    public static final String TRIP = "TRIP";
    public static final String TRIP_STAGE = "TRIP_STAGE";
    public static final String POST = "POST";

    public static final java.util.Set<String> TYPES = java.util.Set.of(
            TEAM, TEAM_ABOUT, TEAM_FAQ, ROUTES_LIST, ROUTE, RIDE, TRIP, TRIP_STAGE, POST);

    @Id
    @Column(name = "team_id")
    private String teamId;

    @Id
    @Column(name = "entity_type", length = 20)
    private String entityType;

    @Id
    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "target_url", nullable = false, length = 1000)
    private String targetUrl;

    public PedalonsRedirect() {
    }

    public PedalonsRedirect(String teamId, String entityType, String entityId, String targetUrl) {
        this.teamId = Objects.requireNonNull(teamId);
        this.entityType = Objects.requireNonNull(entityType);
        this.entityId = Objects.requireNonNull(entityId);
        this.targetUrl = Objects.requireNonNull(targetUrl);
    }

    public String getTeamId() {
        return teamId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public static class Key implements Serializable {

        private String teamId;
        private String entityType;
        private String entityId;

        public Key() {
        }

        public Key(String teamId, String entityType, String entityId) {
            this.teamId = teamId;
            this.entityType = entityType;
            this.entityId = entityId;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(teamId, key.teamId) && Objects.equals(entityType, key.entityType) && Objects.equals(entityId, key.entityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(teamId, entityType, entityId);
        }
    }

}
