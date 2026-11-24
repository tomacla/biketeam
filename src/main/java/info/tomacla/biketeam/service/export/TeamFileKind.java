package info.tomacla.biketeam.service.export;

import java.util.Arrays;
import java.util.Optional;

/**
 * Types de fichiers exportes : nom du FileRef.kind et segment de chemin (endpoint Pedalons
 * /internal/pedalons/teams/{teamId}/files/{kind}/{entityId}, repertoire files/{kind}/ de l'archive ZIP).
 */
public enum TeamFileKind {

    GPX("gpx"),
    RIDE_IMAGE("ride-image"),
    TRIP_IMAGE("trip-image"),
    PUBLICATION_IMAGE("publication-image"),
    LOGO("logo");

    private final String pathSegment;

    TeamFileKind(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String getPathSegment() {
        return pathSegment;
    }

    public static Optional<TeamFileKind> fromPathSegment(String segment) {
        return Arrays.stream(values()).filter(k -> k.pathSegment.equals(segment)).findFirst();
    }

}
