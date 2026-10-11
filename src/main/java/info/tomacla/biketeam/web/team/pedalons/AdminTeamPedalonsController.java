package info.tomacla.biketeam.web.team.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationException;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;

/**
 * Migration du groupe vers Pedalons, depuis son administration (reserve aux administrateurs du
 * groupe par la regle /{teamId}/admin/** de SecurityConfig).
 * <ul>
 *     <li>GET  /{teamId}/admin/pedalons : ce qui sera migre, options, suivi, historique ;</li>
 *     <li>POST /{teamId}/admin/pedalons/start : cree la demande et envoie le navigateur sur
 *     Pedalons avec le jeton signe (303) ;</li>
 *     <li>GET  /{teamId}/admin/pedalons/callback : retour de Pedalons (grant ou annulation),
 *     declenchement M2M, puis retour sur la page de suivi ;</li>
 *     <li>POST /{teamId}/admin/pedalons/switch : bascule d'un groupe pret (migration definitive
 *     reussie) - ses URL redirigent des lors vers Pedalons.</li>
 * </ul>
 */
@Controller
@RequestMapping(value = "/{teamId}/admin/pedalons")
public class AdminTeamPedalonsController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AdminTeamPedalonsController.class);

    @Autowired
    private PedalonsMigrationService migrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public String getPage(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Migration vers Pédalons", team);

        final List<PedalonsMigrationView> migrations = migrationService.history(team.getId()).stream()
                .map(m -> new PedalonsMigrationView(m, getZoneId(team), objectMapper))
                .toList();
        final boolean running = migrations.stream().anyMatch(PedalonsMigrationView::isActive);

        model.addAttribute("pedalonsEnabled", migrationService.isEnabled());
        model.addAttribute("pedalonsMigratable", migrationService.isMigratable(team));
        model.addAttribute("pedalonsState", team.getPedalonsState() == null ? "" : team.getPedalonsState().name());
        model.addAttribute("pedalonsUrl", team.getPedalonsUrl() == null ? "" : team.getPedalonsUrl());
        model.addAttribute("migrations", migrations);
        model.addAttribute("running", running);
        // rafraichissement automatique de la page de suivi tant qu'un job tourne
        model.addAttribute("autoRefresh", running);
        if (migrationService.isEnabled() && !team.isPedalonsMigrated()) {
            model.addAttribute("summary", migrationService.summary(team));
        }

        return "team_admin_pedalons";
    }

    @PostMapping(value = "/start")
    public RedirectView start(@PathVariable("teamId") String teamId,
                              @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
                              @RequestParam(value = "reset", defaultValue = "false") boolean reset,
                              Principal principal,
                              RedirectAttributes attributes) {

        final Team team = checkTeam(teamId);
        final User user = getUserFromPrincipal(principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        try {
            final String target = migrationService.start(team, user, dryRun, reset);
            return seeOther(new RedirectView(target));
        } catch (PedalonsMigrationException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
            return seeOther(viewHandler.redirectView(team, "/admin/pedalons"));
        }
    }

    @PostMapping(value = "/switch")
    public RedirectView switchTeam(@PathVariable("teamId") String teamId,
                                   RedirectAttributes attributes) {

        final Team team = checkTeam(teamId);

        try {
            attributes.addFlashAttribute("infos", List.of(migrationService.switchTeam(team)));
        } catch (PedalonsMigrationException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
        }

        return seeOther(viewHandler.redirectView(team, "/admin/pedalons"));
    }

    @GetMapping(value = "/callback")
    public RedirectView callback(@PathVariable("teamId") String teamId,
                                 @RequestParam(value = "request", required = false) String requestId,
                                 @RequestParam(value = "grant", required = false) String grant,
                                 @RequestParam(value = "outcome", required = false) String outcome,
                                 Principal principal,
                                 HttpServletResponse response,
                                 RedirectAttributes attributes) {

        // le grant est dans l'URL de cette page : il ne doit pas fuiter par l'en-tete Referer
        response.setHeader("Referrer-Policy", "no-referrer");

        final Team team = checkTeam(teamId);

        try {
            attributes.addFlashAttribute("infos", List.of(migrationService.callback(team,
                    getUserFromPrincipal(principal).orElse(null), requestId, grant, outcome)));
        } catch (PedalonsMigrationException e) {
            attributes.addFlashAttribute("errors", List.of(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Pedalons callback failed for team {}", team.getId(), e);
            attributes.addFlashAttribute("errors", List.of("La migration n'a pas pu être lancée."));
        }

        return seeOther(viewHandler.redirectView(team, "/admin/pedalons"));
    }

    private static RedirectView seeOther(RedirectView view) {
        view.setStatusCode(HttpStatus.SEE_OTHER);
        // sans cela les attributs du modele seraient ajoutes a l'URL de redirection
        view.setExposeModelAttributes(false);
        return view;
    }

}
