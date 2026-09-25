package info.tomacla.biketeam.service.pedalons.export;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;

/**
 * Resolution des fichiers exportes vers Pedalons.
 * <p>
 * Le fichier est TOUJOURS deduit de (type, equipe, entite) apres verification que l'entite
 * appartient a l'equipe et n'est pas supprimee : jamais un chemin fourni par l'appelant.
 */
@Component
public class PedalonsExportFiles {

    private static final Logger log = LoggerFactory.getLogger(PedalonsExportFiles.class);

    public static final String GPX_CONTENT_TYPE = "application/gpx+xml";

    /**
     * Extensions d'image, dans l'ordre de recherche : PNG d'abord, puis JPEG sous ses deux formes.
     */
    private static final List<String> IMAGE_EXTENSIONS = List.of(".png", ".jpg", ".jpeg");

    private final FileService fileService;
    private final FileDigestCache digestCache;
    private final MapRepository mapRepository;
    private final RideRepository rideRepository;
    private final TripRepository tripRepository;
    private final PublicationRepository publicationRepository;

    @Autowired
    public PedalonsExportFiles(FileService fileService, FileDigestCache digestCache, MapRepository mapRepository,
                               RideRepository rideRepository, TripRepository tripRepository,
                               PublicationRepository publicationRepository) {
        this.fileService = fileService;
        this.digestCache = digestCache;
        this.mapRepository = mapRepository;
        this.rideRepository = rideRepository;
        this.tripRepository = tripRepository;
        this.publicationRepository = publicationRepository;
    }

    /**
     * Un fichier resolu sur disque.
     */
    public record ExportFile(Path path, String fileName, String contentType) {
    }

    /**
     * Resolution pour l'endpoint de fichiers : verifie l'appartenance de l'entite a l'equipe.
     */
    public Optional<ExportFile> resolve(PedalonsFileKind kind, String teamId, String entityId) {
        return switch (kind) {
            case GPX -> mapRepository.findById(entityId)
                    .filter(m -> teamId.equals(m.getTeamId()) && !m.isDeletion())
                    .flatMap(this::gpx);
            case RIDE_IMAGE -> rideRepository.findById(entityId)
                    .filter(r -> teamId.equals(r.getTeamId()) && !r.isDeletion())
                    .flatMap(r -> image(kind, teamId, r.getId()));
            case TRIP_IMAGE -> tripRepository.findById(entityId)
                    .filter(t -> teamId.equals(t.getTeamId()) && !t.isDeletion())
                    .flatMap(t -> image(kind, teamId, t.getId()));
            case PUBLICATION_IMAGE -> publicationRepository.findById(entityId)
                    .filter(p -> teamId.equals(p.getTeamId()) && !p.isDeletion())
                    .flatMap(p -> image(kind, teamId, p.getId()));
            case LOGO -> teamId.equals(entityId) ? logo(teamId) : Optional.empty();
        };
    }

    public Optional<ExportFile> gpx(Map map) {
        final String fileName = map.getId() + ".gpx";
        if (!fileService.fileExists(FileRepositories.GPX_FILES, map.getTeamId(), fileName)) {
            return Optional.empty();
        }
        return Optional.of(new ExportFile(fileService.getFile(FileRepositories.GPX_FILES, map.getTeamId(), fileName),
                fileName, GPX_CONTENT_TYPE));
    }

    public Optional<ExportFile> image(PedalonsFileKind kind, String teamId, String entityId) {
        final String directory = switch (kind) {
            case RIDE_IMAGE -> FileRepositories.RIDE_IMAGES;
            case TRIP_IMAGE -> FileRepositories.TRIP_IMAGES;
            case PUBLICATION_IMAGE -> FileRepositories.PUBLICATION_IMAGES;
            default -> throw new IllegalArgumentException("Not an image kind: " + kind);
        };
        return findImage(directory, teamId, entityId);
    }

    /**
     * misc/{teamId}/logo.(png|jpg|jpeg), y compris le logo factice pose a la creation de
     * l'equipe : c'est Pedalons qui l'ecarte par empreinte. Jamais misc/logo.png (logo du site).
     */
    public Optional<ExportFile> logo(String teamId) {
        return findImage(FileRepositories.MISC_IMAGES, teamId, "logo");
    }

    public Optional<ExportFile> find(Ride ride) {
        return image(PedalonsFileKind.RIDE_IMAGE, ride.getTeamId(), ride.getId());
    }

    public Optional<ExportFile> find(Trip trip) {
        return image(PedalonsFileKind.TRIP_IMAGE, trip.getTeamId(), trip.getId());
    }

    public Optional<ExportFile> find(Publication publication) {
        return image(PedalonsFileKind.PUBLICATION_IMAGE, publication.getTeamId(), publication.getId());
    }

    /**
     * Fichiers d'une equipe pour la construction d'un instantane : chaque repertoire (GPX, images
     * de sorties, de voyages, de publications, logo) est liste une seule fois, a la premiere
     * demande, puis chaque fichier est cherche dans cette liste - aucun acces disque par element
     * pour savoir si son fichier existe.
     */
    public TeamFiles teamFiles(String teamId) {
        return new TeamFiles(teamId);
    }

    public final class TeamFiles {

        private final String teamId;
        private final java.util.Map<String, java.util.Set<String>> listings = new java.util.HashMap<>();

        private TeamFiles(String teamId) {
            this.teamId = teamId;
        }

        public Optional<ExportFile> gpx(Map map) {
            final String fileName = map.getId() + ".gpx";
            if (!teamId.equals(map.getTeamId()) || !listing(FileRepositories.GPX_FILES).contains(fileName)) {
                return Optional.empty();
            }
            return Optional.of(new ExportFile(fileService.resolveFile(FileRepositories.GPX_FILES, teamId, fileName),
                    fileName, GPX_CONTENT_TYPE));
        }

        public Optional<ExportFile> find(Ride ride) {
            return image(FileRepositories.RIDE_IMAGES, ride.getTeamId(), ride.getId());
        }

        public Optional<ExportFile> find(Trip trip) {
            return image(FileRepositories.TRIP_IMAGES, trip.getTeamId(), trip.getId());
        }

        public Optional<ExportFile> find(Publication publication) {
            return image(FileRepositories.PUBLICATION_IMAGES, publication.getTeamId(), publication.getId());
        }

        public Optional<ExportFile> logo() {
            return image(FileRepositories.MISC_IMAGES, teamId, "logo");
        }

        private Optional<ExportFile> image(String directory, String entityTeamId, String baseName) {
            if (!teamId.equals(entityTeamId)) {
                return Optional.empty();
            }
            final java.util.Set<String> names = listing(directory);
            for (String extension : IMAGE_EXTENSIONS) {
                final String fileName = baseName + extension;
                if (names.contains(fileName)) {
                    final String contentType = ".png".equals(extension) ? "image/png" : "image/jpeg";
                    return Optional.of(new ExportFile(fileService.resolveFile(directory, teamId, fileName), fileName, contentType));
                }
            }
            return Optional.empty();
        }

        private java.util.Set<String> listing(String directory) {
            return listings.computeIfAbsent(directory, d -> fileService.listFileNames(d, teamId));
        }
    }

    private Optional<ExportFile> findImage(String directory, String teamId, String baseName) {
        for (String extension : IMAGE_EXTENSIONS) {
            final String fileName = baseName + extension;
            if (fileService.fileExists(directory, teamId, fileName)) {
                final String contentType = ".png".equals(extension) ? "image/png" : "image/jpeg";
                return Optional.of(new ExportFile(fileService.getFile(directory, teamId, fileName), fileName, contentType));
            }
        }
        return Optional.empty();
    }

    /**
     * Reference du fichier dans l'instantane, taille et empreinte comprises.
     * <p>
     * Null (et un avertissement) si le fichier ne peut pas etre lu : l'element part sans fichier
     * (Pedalons le signale comme fichier manquant) plutot que de faire echouer tout l'instantane.
     */
    public TeamSnapshot.FileRef toRef(PedalonsFileKind kind, String teamId, String entityId, ExportFile file) {
        try {
            // un seul acces disque : taille et date lues ensemble
            final BasicFileAttributes attributes = Files.readAttributes(file.path(), BasicFileAttributes.class);
            final long size = attributes.size();
            final long modified = attributes.lastModifiedTime().toMillis();
            return new TeamSnapshot.FileRef(
                    kind.name(),
                    filePath(teamId, kind, entityId),
                    file.fileName(),
                    file.contentType(),
                    size,
                    digestCache.md5(file.path(), size, modified));
        } catch (IOException | UncheckedIOException e) {
            log.warn("Pedalons export: unreadable {} file {} of {} in team {}, exported without it",
                    kind, file.fileName(), entityId, teamId, e);
            return null;
        }
    }

    public String md5(ExportFile file) {
        try {
            return digestCache.md5(file.path(), Files.size(file.path()), Files.getLastModifiedTime(file.path()).toMillis());
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read " + file.fileName(), e);
        }
    }

    public static String filePath(String teamId, PedalonsFileKind kind, String entityId) {
        return "/internal/pedalons/teams/" + teamId + "/files/" + kind.getPathSegment() + "/" + entityId;
    }

}
