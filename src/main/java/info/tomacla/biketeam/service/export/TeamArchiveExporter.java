package info.tomacla.biketeam.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Archive ZIP du contenu d'une equipe, telechargee par ses administrateurs.
 * <p>
 * Meme contenu que l'instantane Pedalons (aucune donnee de personne : ni utilisateurs, ni roles,
 * ni participants, ni messages), sans les elements supprimes :
 * <pre>
 * README.txt
 * team.json                         instantane, FileRef.path relatif a la racine de l'archive
 * files/{kind}/{fichier}            GPX, images des rides, trips et publications, logo
 * </pre>
 * Deux temps, comme l'instantane : {@link #prepare(Team)} lit la base et les attributs des fichiers
 * avant l'envoi du moindre octet (une erreur donne encore une page d'erreur), puis
 * {@link #write(TeamArchive, OutputStream)} ecrit l'archive au fil de l'eau, sans tenir de
 * connexion a la base ni charger de fichier en memoire.
 */
@Service
public class TeamArchiveExporter {

    private static final Logger log = LoggerFactory.getLogger(TeamArchiveExporter.class);

    public static final String SNAPSHOT_ENTRY = "team.json";
    public static final String README_ENTRY = "README.txt";

    /**
     * files/{kind}/{nom du fichier} : le nom est l'id de l'element suivi de l'extension (logo.png
     * pour le logo), unique pour un type donne.
     */
    public static final TeamExportFiles.FilePaths ZIP_PATHS =
            (kind, teamId, entityId, file) -> "files/" + kind.getPathSegment() + "/" + file.fileName();

    private final TeamSnapshotBuilder snapshotBuilder;
    private final ObjectMapper objectMapper;

    @Autowired
    public TeamArchiveExporter(TeamSnapshotBuilder snapshotBuilder, ObjectMapper objectMapper) {
        this.snapshotBuilder = snapshotBuilder;
        this.objectMapper = objectMapper;
    }

    /**
     * Instantane filtre et fichiers a inclure.
     */
    public record TeamArchive(TeamSnapshot.Snapshot snapshot, List<TeamSnapshotBuilder.ExportedFile> files) {
    }

    public TeamArchive prepare(Team team) {
        final TeamSnapshotBuilder.TeamExport export = snapshotBuilder.build(team, ZIP_PATHS);
        final TeamSnapshot.Snapshot snapshot = withoutDeleted(export.snapshot());
        // les elements supprimes ne referencent aucun fichier : rien a retirer de la liste,
        // mais on ne garde que ce que l'instantane filtre reference
        final Set<String> referenced = referencedPaths(snapshot);
        final List<TeamSnapshotBuilder.ExportedFile> files = export.files().stream()
                .filter(f -> referenced.contains(f.ref().path()))
                .toList();
        return new TeamArchive(snapshot, files);
    }

    public void write(TeamArchive archive, OutputStream out) throws IOException {
        final ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
        final Set<String> written = new HashSet<>();

        zip.setLevel(Deflater.DEFAULT_COMPRESSION);
        putEntry(zip, README_ENTRY, readme(archive).getBytes(StandardCharsets.UTF_8));
        written.add(README_ENTRY);
        // pas d'objectMapper.writeValue(zip, ...) : Jackson fermerait le flux de l'archive
        putEntry(zip, SNAPSHOT_ENTRY, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(archive.snapshot()));
        written.add(SNAPSHOT_ENTRY);

        for (TeamSnapshotBuilder.ExportedFile file : archive.files()) {
            final String name = file.ref().path();
            if (!written.add(name)) {
                continue;
            }
            final InputStream is;
            try {
                is = Files.newInputStream(file.file().path());
            } catch (IOException e) {
                // disparu depuis la preparation : l'archive part sans lui
                log.warn("Team export: file {} vanished, archived without it", file.file().path(), e);
                continue;
            }
            try (is) {
                // images deja compressees : les recompresser ne ferait que couter du temps
                zip.setLevel(file.file().contentType().startsWith("image/") ? Deflater.NO_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
                zip.putNextEntry(new ZipEntry(name));
                is.transferTo(zip);
                zip.closeEntry();
            }
        }

        // finish() et non close() : le flux de la reponse appartient au conteneur
        zip.finish();
        zip.flush();
    }

    private static void putEntry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }

    /**
     * Sans les rides, trips et publications supprimes. Un parcours supprime n'est garde que s'il
     * est encore reference par une ride ou une etape conservee (marque deleted, sans GPX) : la
     * reference mapId ne pointe ainsi jamais dans le vide.
     */
    static TeamSnapshot.Snapshot withoutDeleted(TeamSnapshot.Snapshot snapshot) {
        final List<TeamSnapshot.SnapshotRide> rides = snapshot.rides().stream().filter(r -> !r.deleted()).toList();
        final List<TeamSnapshot.SnapshotTrip> trips = snapshot.trips().stream().filter(t -> !t.deleted()).toList();
        final List<TeamSnapshot.SnapshotPublication> publications = snapshot.publications().stream()
                .filter(p -> !p.deleted()).toList();

        final Set<String> usedMaps = Stream.concat(
                        rides.stream().flatMap(r -> r.groups().stream()).map(TeamSnapshot.SnapshotRideGroup::mapId),
                        trips.stream().flatMap(t -> t.stages().stream()).map(TeamSnapshot.SnapshotTripStage::mapId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final List<TeamSnapshot.SnapshotMap> maps = snapshot.maps().stream()
                .filter(m -> !m.deleted() || usedMaps.contains(m.id()))
                .toList();

        return new TeamSnapshot.Snapshot(snapshot.schemaVersion(), snapshot.generatedAt(), snapshot.team(),
                snapshot.places(), maps, snapshot.rideTemplates(), publications, rides, trips);
    }

    private static Set<String> referencedPaths(TeamSnapshot.Snapshot snapshot) {
        return Stream.of(
                        Stream.of(snapshot.team().logo()),
                        snapshot.maps().stream().map(TeamSnapshot.SnapshotMap::gpx),
                        snapshot.publications().stream().map(TeamSnapshot.SnapshotPublication::image),
                        snapshot.rides().stream().map(TeamSnapshot.SnapshotRide::image),
                        snapshot.trips().stream().map(TeamSnapshot.SnapshotTrip::image))
                .flatMap(s -> s)
                .filter(Objects::nonNull)
                .map(TeamSnapshot.FileRef::path)
                .collect(Collectors.toSet());
    }

    private static String readme(TeamArchive archive) {
        final TeamSnapshot.Snapshot s = archive.snapshot();
        return """
                Export des données du groupe "%s" (%s)
                Généré le %s (UTC) depuis biketeam.

                Contenu
                -------
                team.json   description du groupe, lieux, parcours, modèles de ride,
                            publications, rides et trips (format JSON, schemaVersion %d)
                files/      fichiers référencés par team.json (champ "path") :
                  files/gpx/                 traces GPX des parcours
                  files/ride-image/          images des rides
                  files/trip-image/          images des trips
                  files/publication-image/   images des publications
                  files/logo/                logo du groupe

                %d lieu(x), %d parcours, %d modèle(s) de ride, %d publication(s),
                %d ride(s), %d trip(s), %d fichier(s).

                Notes
                -----
                - Aucune donnée personnelle : ni membres, ni participants, ni messages.
                - Les éléments supprimés ne sont pas inclus. Un parcours supprimé encore
                  utilisé par une ride ou un trip est conservé, marqué "deleted": true.
                - Les dates sont au format ISO 8601 ; publishedAt et generatedAt sont en UTC,
                  les heures de rendez-vous dans le fuseau du groupe.
                - "size" et "md5" de chaque fichier permettent d'en vérifier l'intégrité.
                """.formatted(
                s.team().name(), s.team().id(), s.generatedAt(), s.schemaVersion(),
                s.places().size(), s.maps().size(), s.rideTemplates().size(), s.publications().size(),
                s.rides().size(), s.trips().size(), archive.files().size());
    }

}
