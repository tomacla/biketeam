package info.tomacla.biketeam.security.completion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Forcage de la completion de compte.
 * <p>
 * Branche APRES l'AuthorizationFilter : l'authentification et les regles authorizeHttpRequests
 * sont deja appliquees, une redirection 302 ne peut donc pas masquer un 401 ou un 403.
 */
@Component
public class AccountCompletionFilter extends OncePerRequestFilter {

    /**
     * Chemins toujours accessibles a un compte incomplet.
     * <p>
     * /logout et /users/me/delete sont OBLIGATOIRES : sans eux, un utilisateur qui refuse de
     * completer son compte serait piege, sans pouvoir ni sortir ni se supprimer.
     * /login/oauth2/code/* est couvert par /login/** : c'est precisement le parcours qui fait
     * sortir de l'etat incomplet.
     * /api/** est volontairement hors du filtre : l'application mobile doit continuer a
     * fonctionner pour les comptes Strava incomplets pendant toute la transition.
     */
    public static final List<String> WHITELIST_PATTERNS = List.of(
            "/account/**",
            "/login",
            "/login/**",
            "/logout",
            "/oauth2/authorization/**",
            "/register/**",
            "/verify-email",
            "/forgot-password",
            "/reset-password",
            "/forms/token",
            "/confirm-email",
            "/users/me/delete",
            "/users/*/image",
            "/api/**",
            "/management/**",
            "/auth/**",
            "/gpxtool/**",
            "/error",
            "/css/**",
            "/js/**",
            "/jsf/**",
            "/img/**",
            "/misc/**",
            "/*/image",
            "/legal-mentions",
            "/robots.txt"
    );

    public static final RequestMatcher WHITELIST = new OrRequestMatcher(
            WHITELIST_PATTERNS.stream()
                    .map(pattern -> (RequestMatcher) AntPathRequestMatcher.antMatcher(pattern))
                    .collect(Collectors.toList())
    );

    private static final String COMPLETION_REQUIRED_BODY = "{\"error\":\"account_completion_required\"}";

    @Autowired
    private AccountCompletionService accountCompletionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!accountCompletionService.enforcementEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // preflight CORS : jamais redirige
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (WHITELIST.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (accountCompletionService.isComplete(authentication, request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isJsonRequest(request)) {
            // sans cela les scripts (/autocomplete, /notifications) recevraient une page HTML
            // qu'ils tenteraient de parser comme du JSON
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(COMPLETION_REQUIRED_BODY);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/account/complete");

    }

    private boolean isJsonRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        final String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }

}
