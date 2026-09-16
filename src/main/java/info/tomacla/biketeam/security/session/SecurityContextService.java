package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reconstruction de l'authentification courante.
 * <p>
 * Depuis Spring Security 6, SecurityContextHolderFilter n'enregistre plus automatiquement le
 * contexte modifie en cours de requete : toute modification doit etre suivie d'un appel
 * explicite a {@link SecurityContextRepository#saveContext}. Sans cela, l'ajout d'une autorite
 * ou le rafraichissement du principal est perdu des la requete suivante.
 */
@Service
public class SecurityContextService {

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Value("${rememberme.key}")
    private String rememberMeKey;

    /**
     * Remplace le principal de la session par une vue fraiche de l'utilisateur, en conservant
     * le type de token (OAuth2, remember-me ou form login) et les autorites recalculees.
     */
    public void refreshCurrentUser(User user, HttpServletRequest request, HttpServletResponse response) {

        final Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current == null || user == null) {
            return;
        }

        final OAuth2UserDetails principal = OAuth2UserDetails.create(user);

        saveAuthentication(buildToken(current, principal, principal.getAuthorities()), request, response);

    }

    /**
     * Ajoute une autorite a l'authentification courante (creation d'une team par exemple).
     */
    public void addAuthority(GrantedAuthority authority, HttpServletRequest request, HttpServletResponse response) {

        final Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current == null) {
            return;
        }

        final List<GrantedAuthority> updatedAuthorities = new ArrayList<>(current.getAuthorities());
        if (!updatedAuthorities.contains(authority)) {
            updatedAuthorities.add(authority);
        }

        saveAuthentication(buildToken(current, current.getPrincipal(), updatedAuthorities), request, response);

    }

    private Authentication buildToken(Authentication current,
                                      Object principal,
                                      Collection<? extends GrantedAuthority> authorities) {

        if (current instanceof OAuth2AuthenticationToken oauth2Auth && principal instanceof OAuth2UserDetails details) {
            return new OAuth2AuthenticationToken(details, authorities, oauth2Auth.getAuthorizedClientRegistrationId());
        }

        if (current instanceof RememberMeAuthenticationToken) {
            return new RememberMeAuthenticationToken(rememberMeKey, principal, authorities);
        }

        // le type de jeton est conserve : le retrograder en UsernamePasswordAuthenticationToken
        // ferait passer une session ouverte par passkey pour une session par mot de passe
        if (current instanceof PasskeyAuthenticationToken passkeyAuth) {
            return PasskeyAuthenticationToken.authenticated(principal, passkeyAuth.getPasskeyId(), authorities);
        }

        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);

    }

    private void saveAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
