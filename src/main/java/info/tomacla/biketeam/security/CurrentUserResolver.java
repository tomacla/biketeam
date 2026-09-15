package info.tomacla.biketeam.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Identifiant de l'utilisateur authentifie, hors contexte MVC.
 * <p>
 * Utilise par la chaine OAuth2 : {@code OAuth2LoginAuthenticationFilter} ne vide le
 * SecurityContextHolder qu'en cas d'echec, l'authentification en cours est donc lisible pendant
 * la resolution de l'utilisateur OAuth2.
 * <p>
 * Tous les types de token sont acceptes des lors que le principal est un {@link OAuth2UserDetails},
 * <b>y compris {@code RememberMeAuthenticationToken}</b> : c'est le cas nominal d'un utilisateur
 * Strava qui revient avec un cookie et veut lier son compte a Google ou Facebook.
 */
@Service
public class CurrentUserResolver {

    public Optional<String> currentUserId() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof OAuth2UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }

        return Optional.empty();

    }

}
