package info.tomacla.biketeam.web.pedalons;

import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationRepository;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationStatus;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.pedalons.PedalonsProperties;
import info.tomacla.biketeam.service.pedalons.export.PedalonsExportFiles;
import info.tomacla.biketeam.service.pedalons.export.PedalonsFileKind;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshot;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshotBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API d'export M2M lue par Pedalons, en HTTPS par l'entree publique, pendant une migration.
 * <p>
 * Authentification par l'en-tete X-Pedalons-Export-Secret (comparaison a temps constant), pas de
 * session ni de CSRF (GET seulement). Garde supplementaire : une equipe n'est exportee que
 * pendant une migration active (TRIGGERING ou RUNNING) - le secret seul ne permet pas d'aspirer
 * n'importe quelle equipe. Pas de filtrage reseau (pas de VPN) : le secret et cette garde suffisent.
 * <p>
 * Fonction desactivee : 404 sans corps, comme une route inexistante.
 */
@RestController
@RequestMapping("/internal/pedalons")
public class PedalonsExportController {

    private static final Logger log = LoggerFactory.getLogger(PedalonsExportController.class);

    public static final String SECRET_HEADER = "X-Pedalons-Export-Secret";

    private final PedalonsProperties properties;
    private final TeamService teamService;
    private final PedalonsMigrationRepository migrationRepository;
    private final TeamSnapshotBuilder snapshotBuilder;
    private final PedalonsExportFiles files;

    @Autowired
    public PedalonsExportController(PedalonsProperties properties, TeamService teamService,
                                    PedalonsMigrationRepository migrationRepository,
                                    TeamSnapshotBuilder snapshotBuilder, PedalonsExportFiles files) {
        this.properties = properties;
        this.teamService = teamService;
        this.migrationRepository = migrationRepository;
        this.snapshotBuilder = snapshotBuilder;
        this.files = files;
    }

    @GetMapping(path = "/teams/{teamId}/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamSnapshot.Snapshot getSnapshot(@PathVariable("teamId") String teamId,
                                             @RequestHeader(value = SECRET_HEADER, required = false) String secret) {
        final Team team = checkAccess(teamId, secret);
        log.info("Pedalons export: snapshot of team {}", team.getId());
        return snapshotBuilder.build(team);
    }

    @GetMapping(path = "/teams/{teamId}/files/{kind}/{entityId}")
    public ResponseEntity<Resource> getFile(@PathVariable("teamId") String teamId,
                                            @PathVariable("kind") String kind,
                                            @PathVariable("entityId") String entityId,
                                            @RequestHeader(value = SECRET_HEADER, required = false) String secret) throws IOException {

        final Team team = checkAccess(teamId, secret);

        final PedalonsExportFiles.ExportFile file = PedalonsFileKind.fromPathSegment(kind)
                .flatMap(k -> files.resolve(k, team.getId(), entityId))
                .orElseThrow(() -> new ExportException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "File not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(Files.size(file.path()))
                .eTag("\"" + files.md5(file) + "\"")
                .body(new FileSystemResource(file.path()));
    }

    private Team checkAccess(String teamId, String secret) {

        if (!properties.isEnabled()) {
            throw new ExportException(HttpStatus.NOT_FOUND, null, null);
        }

        if (!properties.isValidExportSecret(secret)) {
            throw new ExportException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid " + SECRET_HEADER);
        }

        final Team team = teamService.get(teamId)
                .filter(t -> !t.isDeletion())
                .orElseThrow(() -> new ExportException(HttpStatus.NOT_FOUND, "TEAM_NOT_FOUND", "Unknown team"));

        if (!migrationRepository.existsByTeamIdAndStatusIn(team.getId(), PedalonsMigrationStatus.ACTIVE)) {
            throw new ExportException(HttpStatus.CONFLICT, "NO_ACTIVE_MIGRATION", "No active migration for this team");
        }

        return team;
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<Map<String, String>> handleExportException(ExportException e) {
        if (e.code == null) {
            return ResponseEntity.status(e.status).build();
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", e.code);
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /**
     * Toute autre erreur : 500 JSON, que Pedalons traite comme une panne transitoire (nouvel
     * essai, puis EXPORT_UNAVAILABLE). Sans ce gestionnaire, WebExceptionHandler repondrait par une
     * redirection vers l'accueil (et une session), lue par Pedalons comme un refus definitif.
     * Les exceptions qui portent deja un statut (405, 406, parametre invalide...) le conservent.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(HttpServletRequest request, HttpServletResponse response,
                                                               Exception e) {
        final HttpStatusCode status;
        if (e instanceof ErrorResponse errorResponse) {
            status = errorResponse.getStatusCode();
            log.warn("Pedalons export: {} {} answered {}: {}", request.getMethod(), request.getRequestURI(),
                    status.value(), e.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("Pedalons export: {} {} failed", request.getMethod(), request.getRequestURI(), e);
        }
        if (response.isCommitted()) {
            // flux de fichier interrompu (client parti...) : plus rien a ecrire
            return null;
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", status.is5xxServerError() ? "INTERNAL_ERROR" : "BAD_REQUEST");
        body.put("message", status.is5xxServerError() ? "Export failed" : "Invalid request");
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    static class ExportException extends RuntimeException {

        private final HttpStatus status;
        private final String code;

        ExportException(HttpStatus status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }
    }

}
