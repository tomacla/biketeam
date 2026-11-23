package info.tomacla.biketeam.web.team.export;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.export.TeamArchiveExporter;
import info.tomacla.biketeam.service.export.TeamSnapshotBuilder;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Export des donnees du groupe par ses administrateurs (regle /{teamId}/admin/** de SecurityConfig).
 * <ul>
 *     <li>GET /{teamId}/admin/export : ce que contient l'export ;</li>
 *     <li>GET /{teamId}/admin/export/download : l'archive ZIP (voir {@link TeamArchiveExporter}).</li>
 * </ul>
 */
@Controller
@RequestMapping(value = "/{teamId}/admin/export")
public class AdminTeamExportController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AdminTeamExportController.class);

    public static final String ZIP_CONTENT_TYPE = "application/zip";

    @Autowired
    private TeamSnapshotBuilder snapshotBuilder;

    @Autowired
    private TeamArchiveExporter archiveExporter;

    private Clock clock = Clock.systemUTC();

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @GetMapping
    public String getPage(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Exporter les données", team);
        model.addAttribute("summary", snapshotBuilder.summary(team));

        return "team_admin_export";
    }

    /**
     * Ecrit directement dans la reponse (pas de StreamingResponseBody) : une archive de plusieurs
     * centaines de Mo sur une connexion lente ne doit pas buter sur le delai des requetes asynchrones.
     */
    @GetMapping(value = "/download")
    public void download(@PathVariable("teamId") String teamId,
                         Principal principal,
                         HttpServletResponse response) throws IOException {

        final Team team = checkTeam(teamId);
        final long start = System.currentTimeMillis();

        // base et fichiers lus avant le premier octet : une erreur ici donne une page d'erreur
        final TeamArchiveExporter.TeamArchive archive = archiveExporter.prepare(team);

        response.setContentType(ZIP_CONTENT_TYPE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(fileName(team)).build().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        try {
            archiveExporter.write(archive, response.getOutputStream());
        } catch (IOException e) {
            // client parti en cours de telechargement, le plus souvent : plus rien a lui ecrire
            log.warn("Team export: archive of team {} interrupted: {}", team.getId(), e.getMessage());
            return;
        }

        log.info("Team export: team {} exported by {} ({} files, {} ms)", team.getId(),
                principal == null ? "?" : principal.getName(), archive.files().size(),
                System.currentTimeMillis() - start);
    }

    String fileName(Team team) {
        return "biketeam-" + team.getId().replaceAll("[^A-Za-z0-9_-]", "_") + "-" + LocalDate.now(clock) + ".zip";
    }

}
