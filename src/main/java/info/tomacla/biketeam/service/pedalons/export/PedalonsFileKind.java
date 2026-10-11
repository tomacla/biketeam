package info.tomacla.biketeam.service.pedalons.export;

import java.util.Arrays;
import java.util.Optional;

/**
 * Types de fichiers servis par l'export : nom du FileRef.kind et segment de chemin de
 * /internal/pedalons/teams/{teamId}/files/{kind}/{entityId}.
 */
public enum PedalonsFileKind {

    GPX("gpx"),
    RIDE_IMAGE("ride-image"),
    TRIP_IMAGE("trip-image"),
    PUBLICATION_IMAGE("publication-image"),
    LOGO("logo");

    private final String pathSegment;

    PedalonsFileKind(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String getPathSegment() {
        return pathSegment;
    }

    public static Optional<PedalonsFileKind> fromPathSegment(String segment) {
        return Arrays.stream(values()).filter(k -> k.pathSegment.equals(segment)).findFirst();
    }

}
