package info.tomacla.biketeam.service.pedalons.export;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.place.PlaceRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamDescription;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.trip.TripStage;
import info.tomacla.biketeam.service.pedalons.PedalonsSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Construit l'instantane d'une equipe pour Pedalons (GET /internal/pedalons/teams/{teamId}/snapshot).
 * <p>
 * Couvre le contenu de l'equipe et rien de ce qui touche aux personnes : ni utilisateurs, ni roles,
 * ni participants, ni messages, ni inscriptions, ni notes ou favoris de parcours.
 * <p>
 * Ordre : tableaux de premier niveau tries par id (determinisme) ; tableaux imbriques dans
 * l'ordre d'affichage de biketeam, rendu total - l'index devient le sortOrder cote Pedalons.
 */
@Service
public class TeamSnapshotBuilder {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Ride.getSortedGroups() : heure de rendez-vous, puis nom. Les valeurs nulles (que
     * getSortedGroups() ne supporte pas) passent en dernier, l'id departage les egalites exactes.
     */
    public static final Comparator<RideGroup> RIDE_GROUP_ORDER = Comparator
            .comparing(RideGroup::getMeetingTime, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder()))
            .thenComparing(RideGroup::getName, Comparator.nullsLast(Comparator.<String>naturalOrder()))
            .thenComparing(RideGroup::getId, Comparator.nullsLast(Comparator.<String>naturalOrder()));

    /**
     * Trip.getSortedStages() : date, puis nom, puis id.
     */
    public static final Comparator<TripStage> TRIP_STAGE_ORDER = Comparator
            .comparing(TripStage::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(TripStage::getName, Comparator.nullsLast(Comparator.<String>naturalOrder()))
            .thenComparing(TripStage::getId, Comparator.nullsLast(Comparator.<String>naturalOrder()));

    /**
     * RideTemplate.getSortedGroups() : nom seul, puis id.
     */
    public static final Comparator<RideGroupTemplate> RIDE_GROUP_TEMPLATE_ORDER = Comparator
            .comparing(RideGroupTemplate::getName, Comparator.nullsLast(Comparator.<String>naturalOrder()))
            .thenComparing(RideGroupTemplate::getId, Comparator.nullsLast(Comparator.<String>naturalOrder()));

    private final PlaceRepository placeRepository;
    private final MapRepository mapRepository;
    private final RideTemplateRepository rideTemplateRepository;
    private final PublicationRepository publicationRepository;
    private final RideRepository rideRepository;
    private final TripRepository tripRepository;
    private final PedalonsExportFiles files;
    private final TransactionTemplate readOnlyTx;

    private Clock clock = Clock.systemUTC();

    @Autowired
    public TeamSnapshotBuilder(PlaceRepository placeRepository, MapRepository mapRepository,
                               RideTemplateRepository rideTemplateRepository, PublicationRepository publicationRepository,
                               RideRepository rideRepository, TripRepository tripRepository, PedalonsExportFiles files,
                               PlatformTransactionManager transactionManager) {
        this.placeRepository = placeRepository;
        this.mapRepository = mapRepository;
        this.rideTemplateRepository = rideTemplateRepository;
        this.publicationRepository = publicationRepository;
        this.rideRepository = rideRepository;
        this.tripRepository = tripRepository;
        this.files = files;
        this.readOnlyTx = new TransactionTemplate(transactionManager);
        this.readOnlyTx.setReadOnly(true);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Deux temps : les entites sont lues et converties en valeurs simples (les records de
     * l'instantane) dans une transaction courte en lecture seule, qui localise aussi les fichiers ;
     * les references de fichiers (taille, md5 - lecture complete de chaque fichier absent du cache)
     * sont calculees ensuite, hors transaction : aucune connexion n'est tenue pendant ces lectures,
     * qui precedent l'envoi du moindre en-tete de reponse.
     */
    public TeamSnapshot.Snapshot build(Team team) {
        final PendingFiles pending = new PendingFiles();
        final TeamSnapshot.Snapshot draft = readOnlyTx.execute(status -> draft(team, pending));
        return pending.resolve(draft);
    }

    private TeamSnapshot.Snapshot draft(Team team, PendingFiles pending) {

        final String teamId = team.getId();
        // une requete par type d'element (graphes de chargement des depots), une liste par
        // repertoire de fichiers : rien par element
        final PedalonsExportFiles.TeamFiles teamFiles = files.teamFiles(teamId);

        return new TeamSnapshot.Snapshot(
                TeamSnapshot.SCHEMA_VERSION,
                clock.instant().toString(),
                team(team, teamFiles, pending),
                sortedById(placeRepository.findByTeamId(teamId), Place::getId).stream().map(this::place).toList(),
                sortedById(mapRepository.findByTeamId(teamId), Map::getId).stream().map(m -> map(m, teamFiles, pending)).toList(),
                sortedById(rideTemplateRepository.findByTeamId(teamId), RideTemplate::getId).stream().map(this::rideTemplate).toList(),
                sortedById(publicationRepository.findByTeamId(teamId), Publication::getId).stream().map(p -> publication(p, teamFiles, pending)).toList(),
                sortedById(rideRepository.findByTeamId(teamId), Ride::getId).stream().map(r -> ride(r, teamFiles, pending)).toList(),
                sortedById(tripRepository.findByTeamId(teamId), Trip::getId).stream().map(t -> trip(t, teamFiles, pending)).toList());
    }

    /**
     * Fichiers localises pendant la transaction, references calculees apres : chaque element
     * porte d'abord une reference provisoire (sans taille ni md5), remplacee par la vraie - ou
     * par null si le fichier ne peut pas etre lu - hors transaction.
     */
    private final class PendingFiles {

        private record Request(PedalonsFileKind kind, String teamId, String entityId, PedalonsExportFiles.ExportFile file) {
        }

        private final java.util.Map<TeamSnapshot.FileRef, Request> requests = new IdentityHashMap<>();

        TeamSnapshot.FileRef defer(PedalonsFileKind kind, String teamId, String entityId, PedalonsExportFiles.ExportFile file) {
            final TeamSnapshot.FileRef placeholder = new TeamSnapshot.FileRef(kind.name(),
                    PedalonsExportFiles.filePath(teamId, kind, entityId), file.fileName(), file.contentType(), -1, null);
            requests.put(placeholder, new Request(kind, teamId, entityId, file));
            return placeholder;
        }

        private TeamSnapshot.FileRef resolve(TeamSnapshot.FileRef placeholder) {
            if (placeholder == null) {
                return null;
            }
            final Request request = requests.get(placeholder);
            return files.toRef(request.kind(), request.teamId(), request.entityId(), request.file());
        }

        TeamSnapshot.Snapshot resolve(TeamSnapshot.Snapshot draft) {
            final TeamSnapshot.SnapshotTeam team = draft.team();
            return new TeamSnapshot.Snapshot(
                    draft.schemaVersion(),
                    draft.generatedAt(),
                    team.logo() == null ? team : team.withLogo(resolve(team.logo())),
                    draft.places(),
                    draft.maps().stream().map(m -> m.gpx() == null ? m : m.withGpx(resolve(m.gpx()))).toList(),
                    draft.rideTemplates(),
                    draft.publications().stream().map(p -> p.image() == null ? p : p.withImage(resolve(p.image()))).toList(),
                    draft.rides().stream().map(r -> r.image() == null ? r : r.withImage(resolve(r.image()))).toList(),
                    draft.trips().stream().map(t -> t.image() == null ? t : t.withImage(resolve(t.image()))).toList());
        }
    }

    /**
     * Compteurs du jeton de demande : elements non supprimes, sans lire les fichiers.
     */
    @Transactional(readOnly = true)
    public PedalonsSummary summary(Team team) {
        final String teamId = team.getId();
        return new PedalonsSummary(
                placeRepository.countByTeamId(teamId),
                mapRepository.countByTeamIdAndDeletionFalse(teamId),
                rideRepository.countByTeamIdAndDeletionFalse(teamId),
                rideTemplateRepository.countByTeamId(teamId),
                tripRepository.countByTeamIdAndDeletionFalse(teamId),
                tripRepository.countStagesOfActiveTrips(teamId),
                publicationRepository.countByTeamIdAndDeletionFalse(teamId),
                team.getConfiguration() != null && team.getConfiguration().isMarkdownPageWritten(),
                files.logo(teamId).isPresent());
    }

    private TeamSnapshot.SnapshotTeam team(Team team, PedalonsExportFiles.TeamFiles teamFiles, PendingFiles pending) {
        final TeamDescription d = team.getDescription();
        final TeamSnapshot.SnapshotTeamDescription description = d == null ? null : new TeamSnapshot.SnapshotTeamDescription(
                d.getDescription(),
                d.getAddressStreetLine(),
                d.getAddressPostalCode(),
                d.getAddressCity(),
                d.getPhoneNumber(),
                d.getEmail(),
                d.getFacebook(),
                d.getTwitter(),
                d.getInstagram(),
                d.getOther());

        final String markdownPage = team.getConfiguration() == null ? null : team.getConfiguration().getMarkdownPage();
        final String timezone = team.getConfiguration() == null ? null : team.getConfiguration().getTimezone();

        return new TeamSnapshot.SnapshotTeam(
                team.getId(),
                team.getName(),
                team.getCity(),
                name(team.getCountry()),
                team.getCreatedAt() == null ? null : team.getCreatedAt().toString(),
                name(team.getVisibility()),
                timezone,
                description,
                Strings.isBlank(markdownPage) ? null : markdownPage,
                teamFiles.logo()
                        .map(f -> pending.defer(PedalonsFileKind.LOGO, team.getId(), team.getId(), f))
                        .orElse(null));
    }

    private TeamSnapshot.SnapshotPlace place(Place place) {
        final Point point = place.getPoint();
        return new TeamSnapshot.SnapshotPlace(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getLink(),
                point == null ? null : point.getLat(),
                point == null ? null : point.getLng(),
                place.isStartPlace(),
                place.isEndPlace());
    }

    private TeamSnapshot.SnapshotMap map(Map map, PedalonsExportFiles.TeamFiles teamFiles, PendingFiles pending) {
        final TeamSnapshot.FileRef gpx = map.isDeletion() ? null : teamFiles.gpx(map)
                .map(f -> pending.defer(PedalonsFileKind.GPX, map.getTeamId(), map.getId(), f))
                .orElse(null);
        final List<String> tags = map.getTags() == null ? List.of() : map.getTags().stream()
                .filter(t -> !Strings.isBlank(t))
                .toList();
        return new TeamSnapshot.SnapshotMap(
                map.getId(),
                map.getPermalink(),
                map.getName(),
                map.getLength(),
                name(map.getType()),
                map.getPositiveElevation(),
                map.getNegativeElevation(),
                map.getPostedAt() == null ? null : map.getPostedAt().toString(),
                latLng(map.getStartPoint()),
                latLng(map.getEndPoint()),
                name(map.getWindDirection()),
                tags,
                map.isDeletion(),
                gpx);
    }

    private TeamSnapshot.SnapshotRideTemplate rideTemplate(RideTemplate template) {
        return new TeamSnapshot.SnapshotRideTemplate(
                template.getId(),
                template.getName(),
                template.getDescription(),
                name(template.getType()),
                template.getIncrement(),
                placeId(template.getStartPlace()),
                placeId(template.getEndPlace()),
                sorted(template.getGroups(), RIDE_GROUP_TEMPLATE_ORDER).stream()
                        .map(g -> new TeamSnapshot.SnapshotRideGroupTemplate(
                                g.getId(), g.getName(), g.getAverageSpeed(), time(g.getMeetingTime())))
                        .toList());
    }

    private TeamSnapshot.SnapshotPublication publication(Publication publication, PedalonsExportFiles.TeamFiles teamFiles,
                                                         PendingFiles pending) {
        return new TeamSnapshot.SnapshotPublication(
                publication.getId(),
                publication.getTitle(),
                publication.getContent(),
                name(publication.getPublishedStatus()),
                instant(publication.getPublishedAt()),
                publication.isDeletion(),
                publication.isDeletion() ? null : teamFiles.find(publication)
                        .map(f -> pending.defer(PedalonsFileKind.PUBLICATION_IMAGE, publication.getTeamId(), publication.getId(), f))
                        .orElse(null));
    }

    private TeamSnapshot.SnapshotRide ride(Ride ride, PedalonsExportFiles.TeamFiles teamFiles, PendingFiles pending) {
        return new TeamSnapshot.SnapshotRide(
                ride.getId(),
                ride.getPermalink(),
                ride.getDate() == null ? null : ride.getDate().toString(),
                ride.getTitle(),
                ride.getDescription(),
                name(ride.getType()),
                name(ride.getPublishedStatus()),
                instant(ride.getPublishedAt()),
                placeId(ride.getStartPlace()),
                placeId(ride.getEndPlace()),
                ride.isListedInFeed(),
                ride.isDeletion(),
                ride.isDeletion() ? null : teamFiles.find(ride)
                        .map(f -> pending.defer(PedalonsFileKind.RIDE_IMAGE, ride.getTeamId(), ride.getId(), f))
                        .orElse(null),
                // sans les participants
                sorted(ride.getGroups(), RIDE_GROUP_ORDER).stream()
                        .map(g -> new TeamSnapshot.SnapshotRideGroup(
                                g.getId(), g.getName(), g.getAverageSpeed(), time(g.getMeetingTime()),
                                g.getMap() == null ? null : g.getMap().getId()))
                        .toList());
    }

    private TeamSnapshot.SnapshotTrip trip(Trip trip, PedalonsExportFiles.TeamFiles teamFiles, PendingFiles pending) {
        return new TeamSnapshot.SnapshotTrip(
                trip.getId(),
                trip.getPermalink(),
                trip.getStartDate() == null ? null : trip.getStartDate().toString(),
                trip.getEndDate() == null ? null : trip.getEndDate().toString(),
                time(trip.getMeetingTime()),
                name(trip.getType()),
                name(trip.getPublishedStatus()),
                instant(trip.getPublishedAt()),
                trip.getTitle(),
                trip.getDescription(),
                placeId(trip.getStartPlace()),
                placeId(trip.getEndPlace()),
                Strings.isBlank(trip.getMarkdownPage()) ? null : trip.getMarkdownPage(),
                trip.isListedInFeed(),
                trip.isDeletion(),
                trip.isDeletion() ? null : teamFiles.find(trip)
                        .map(f -> pending.defer(PedalonsFileKind.TRIP_IMAGE, trip.getTeamId(), trip.getId(), f))
                        .orElse(null),
                // sans les participants
                sorted(trip.getStages(), TRIP_STAGE_ORDER).stream()
                        .map(s -> new TeamSnapshot.SnapshotTripStage(
                                s.getId(),
                                s.getDate() == null ? null : s.getDate().toString(),
                                s.getName(),
                                s.getMap() == null ? null : s.getMap().getId(),
                                s.isAlternative()))
                        .toList());
    }

    private static <T> List<T> sortedById(Collection<T> items, Function<T, String> id) {
        return sorted(items, Comparator.comparing(id, Comparator.nullsLast(Comparator.<String>naturalOrder())));
    }

    private static <T> List<T> sorted(Collection<T> items, Comparator<? super T> comparator) {
        if (items == null) {
            return List.of();
        }
        return items.stream().filter(Objects::nonNull).sorted(comparator).toList();
    }

    private static TeamSnapshot.LatLng latLng(Point point) {
        return point == null ? null : new TeamSnapshot.LatLng(point.getLat(), point.getLng());
    }

    private static String placeId(Place place) {
        return place == null ? null : place.getId();
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    public static String time(LocalTime time) {
        return time == null ? null : TIME_FORMAT.format(time);
    }

    public static String instant(ZonedDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant().toString();
    }

}
