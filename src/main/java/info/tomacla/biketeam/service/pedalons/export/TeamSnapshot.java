package info.tomacla.biketeam.service.pedalons.export;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Instantane JSON d'une equipe servi a Pedalons (schemaVersion 1).
 * <p>
 * Tout champ documente est toujours present, null quand il n'a pas de valeur ; tableaux vides
 * plutot que null. Les dates et heures sont deja formatees en chaines (LocalDate.toString(),
 * "HH:mm:ss" secondes comprises, Instant.toString()) pour ne dependre d'aucun reglage de Jackson.
 * Aucune donnee de personne : ni utilisateurs, ni participants, ni messages, ni inscriptions.
 */
public final class TeamSnapshot {

    public static final int SCHEMA_VERSION = 1;

    private TeamSnapshot() {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Snapshot(int schemaVersion,
                           String generatedAt,
                           SnapshotTeam team,
                           List<SnapshotPlace> places,
                           List<SnapshotMap> maps,
                           List<SnapshotRideTemplate> rideTemplates,
                           List<SnapshotPublication> publications,
                           List<SnapshotRide> rides,
                           List<SnapshotTrip> trips) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotTeam(String id,
                               String name,
                               String city,
                               String country,
                               String createdAt,
                               String visibility,
                               String timezone,
                               SnapshotTeamDescription description,
                               String markdownPage,
                               FileRef logo) {

        public SnapshotTeam withLogo(FileRef logo) {
            return new SnapshotTeam(id, name, city, country, createdAt, visibility, timezone, description, markdownPage, logo);
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotTeamDescription(String description,
                                          String addressStreetLine,
                                          String addressPostalCode,
                                          String addressPostalCity,
                                          String phoneNumber,
                                          String email,
                                          String facebook,
                                          String twitter,
                                          String instagram,
                                          String other) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotPlace(String id,
                                String name,
                                String address,
                                String link,
                                Double lat,
                                Double lng,
                                boolean startPlace,
                                boolean endPlace) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record LatLng(double lat, double lng) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotMap(String id,
                              String permalink,
                              String name,
                              double length,
                              String type,
                              double positiveElevation,
                              double negativeElevation,
                              String postedAt,
                              LatLng startPoint,
                              LatLng endPoint,
                              String windDirection,
                              List<String> tags,
                              boolean deleted,
                              FileRef gpx) {

        public SnapshotMap withGpx(FileRef gpx) {
            return new SnapshotMap(id, permalink, name, length, type, positiveElevation, negativeElevation, postedAt,
                    startPoint, endPoint, windDirection, tags, deleted, gpx);
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotRideTemplate(String id,
                                       String name,
                                       String description,
                                       String type,
                                       Integer increment,
                                       String startPlaceId,
                                       String endPlaceId,
                                       List<SnapshotRideGroupTemplate> groups) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotRideGroupTemplate(String id,
                                            String name,
                                            double averageSpeed,
                                            String meetingTime) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotPublication(String id,
                                      String title,
                                      String content,
                                      String publishedStatus,
                                      String publishedAt,
                                      boolean deleted,
                                      FileRef image) {

        public SnapshotPublication withImage(FileRef image) {
            return new SnapshotPublication(id, title, content, publishedStatus, publishedAt, deleted, image);
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotRide(String id,
                               String permalink,
                               String date,
                               String title,
                               String description,
                               String type,
                               String publishedStatus,
                               String publishedAt,
                               String startPlaceId,
                               String endPlaceId,
                               boolean listedInFeed,
                               boolean deleted,
                               FileRef image,
                               List<SnapshotRideGroup> groups) {

        public SnapshotRide withImage(FileRef image) {
            return new SnapshotRide(id, permalink, date, title, description, type, publishedStatus, publishedAt,
                    startPlaceId, endPlaceId, listedInFeed, deleted, image, groups);
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotRideGroup(String id,
                                    String name,
                                    double averageSpeed,
                                    String meetingTime,
                                    String mapId) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotTrip(String id,
                               String permalink,
                               String startDate,
                               String endDate,
                               String meetingTime,
                               String type,
                               String publishedStatus,
                               String publishedAt,
                               String title,
                               String description,
                               String startPlaceId,
                               String endPlaceId,
                               String markdownPage,
                               boolean listedInFeed,
                               boolean deleted,
                               FileRef image,
                               List<SnapshotTripStage> stages) {

        public SnapshotTrip withImage(FileRef image) {
            return new SnapshotTrip(id, permalink, startDate, endDate, meetingTime, type, publishedStatus, publishedAt,
                    title, description, startPlaceId, endPlaceId, markdownPage, listedInFeed, deleted, image, stages);
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SnapshotTripStage(String id,
                                    String date,
                                    String name,
                                    String mapId,
                                    boolean alternative) {
    }

    /**
     * Reference d'un fichier a telecharger sur l'endpoint de fichiers. path est relatif a l'hote ;
     * size et md5 portent sur l'integralite du fichier.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record FileRef(String kind,
                          String path,
                          String fileName,
                          String contentType,
                          long size,
                          String md5) {
    }

}
