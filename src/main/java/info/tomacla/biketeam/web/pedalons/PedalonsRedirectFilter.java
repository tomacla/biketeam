package info.tomacla.biketeam.web.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redirections des groupes migres vers Pedalons, et lecture seule pendant une migration definitive
 * puis jusqu'a la bascule.
 * <p>
 * Enregistre AVANT la chaine Spring Security (voir SecurityConfig) : un groupe prive redirige
 * sans demander de connexion, Pedalons applique ses propres droits.
 * <ul>
 *     <li>groupe bascule : GET/HEAD -> redirection (302 par defaut, 301 si
 *     PEDALONS_REDIRECT_STATUS=301) vers l'equivalent Pedalons (query string abandonnee), autres
 *     methodes -> 410 avec le lien ;</li>
 *     <li>groupe bascule, API (/api/teams/{t}/**, toute methode) -> 410 JSON
 *     {"code":"TEAM_MIGRATED","message":...,"url":...} ;</li>
 *     <li>groupe gele (migration definitive en cours ou en attente de bascule) : toute ecriture
 *     -> 423, en page HTML, ou pour l'API en JSON {"code":"TEAM_FROZEN","message":...,"url":...}
 *     (url : page Pedalons si deja connue, sinon null).</li>
 * </ul>
 */
public class PedalonsRedirectFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PedalonsRedirectFilter.class);

    public static final int SC_LOCKED = 423;

    /**
     * Meme resolution de chemin que Spring MVC (ant_path_matcher) : decode, sans contenu apres ';',
     * sans '//' - sinon /%6E-peloton/join ou /n-peloton;x/join echapperaient au filtre et seraient
     * quand meme routes vers l'equipe n-peloton. UTF-8 si la requete n'a pas d'encodage (le
     * CharacterEncodingFilter de Spring Boot, execute avant, en pose un).
     */
    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    static {
        PATH_HELPER.setDefaultEncoding(StandardCharsets.UTF_8.name());
    }

    public static final String TEAM_MIGRATED = "TEAM_MIGRATED";
    public static final String TEAM_FROZEN = "TEAM_FROZEN";

    private static final ObjectMapper JSON = new ObjectMapper();

    private final PedalonsRedirectResolver resolver;
    private final int redirectStatus;

    /**
     * @param redirectStatus 301 ou 302 (PedalonsProperties.getRedirectStatus, valide au demarrage)
     */
    public PedalonsRedirectFilter(PedalonsRedirectResolver resolver, int redirectStatus) {
        this.resolver = resolver;
        this.redirectStatus = redirectStatus;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String path;
        try {
            path = PATH_HELPER.getPathWithinApplication(request);
        } catch (IllegalArgumentException e) {
            // encodage invalide : Spring MVC ne saura pas davantage router la requete
            log.debug("Undecodable request path {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        final PedalonsRedirectResolver.Decision decision;
        try {
            decision = resolver.resolve(request.getMethod(), path);
        } catch (RuntimeException e) {
            // une panne de la resolution ne doit pas rendre biketeam inaccessible
            log.error("Unable to resolve Pedalons redirect for {}", path, e);
            chain.doFilter(request, response);
            return;
        }

        switch (decision.action()) {
            case REDIRECT -> {
                final String method = request.getMethod();
                if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
                    response.setStatus(redirectStatus);
                    response.setHeader("Location", decision.url());
                } else {
                    writePage(response, HttpServletResponse.SC_GONE,
                            "Ce groupe a déménagé sur Pédalons",
                            "Ce groupe est désormais sur Pédalons, cette action n'est plus possible ici.",
                            decision.url(), "Continuer sur Pédalons");
                }
            }
            case API_GONE -> writeJson(response, HttpServletResponse.SC_GONE, TEAM_MIGRATED,
                    "Ce groupe a déménagé sur Pédalons.", decision.url());
            case API_LOCKED -> writeJson(response, SC_LOCKED, TEAM_FROZEN,
                    "Ce groupe migre vers Pédalons : il est en lecture seule jusqu'à sa bascule.", decision.url());
            case LOCKED -> {
                if (decision.state() == PedalonsState.READY) {
                    writePage(response, SC_LOCKED,
                            "Migration vers Pédalons terminée",
                            "La migration de ce groupe vers Pédalons est terminée, en attente de bascule par l'administrateur du groupe : "
                                    + "il reste en lecture seule ici jusque-là.",
                            decision.url(), "Voir le groupe sur Pédalons");
                } else {
                    writePage(response, SC_LOCKED,
                            "Migration vers Pédalons en cours",
                            "La migration de ce groupe vers Pédalons est en cours : il est en lecture seule jusqu'à la fin de la migration.",
                            decision.url(), "Voir le groupe sur Pédalons");
                }
            }
            default -> chain.doFilter(request, response);
        }
    }

    private static void writeJson(HttpServletResponse response, int status, String code, String message, String url)
            throws IOException {
        final Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("url", url);
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.writeValueAsString(body));
    }

    private static void writePage(HttpServletResponse response, int status, String title, String text, String url,
                                  String linkLabel) throws IOException {
        response.setStatus(status);
        response.setContentType("text/html");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        final String link = url == null ? "" : "<p><a href=\"" + HtmlUtils.htmlEscape(url, "UTF-8") + "\">" + HtmlUtils.htmlEscape(linkLabel, "UTF-8") + "</a></p>";
        response.getWriter().write("<!doctype html><html lang=\"fr\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + HtmlUtils.htmlEscape(title, "UTF-8") + "</title></head>"
                + "<body style=\"font-family:sans-serif;max-width:40em;margin:3em auto;padding:0 1em\">"
                + "<h1>" + HtmlUtils.htmlEscape(title, "UTF-8") + "</h1><p>" + HtmlUtils.htmlEscape(text, "UTF-8") + "</p>" + link
                + "</body></html>");
    }

}
