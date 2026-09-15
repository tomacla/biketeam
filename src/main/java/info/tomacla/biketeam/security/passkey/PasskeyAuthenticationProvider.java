package info.tomacla.biketeam.security.passkey;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Resolution du compte apres verification d'une assertion WebAuthn.
 * <p>
 * Calque de {@link info.tomacla.biketeam.security.password.EmailPasswordAuthenticationProvider} :
 * le principal pose reste un {@link OAuth2UserDetails} dont getUsername() est l'identifiant
 * utilisateur, pour que le cookie remember-me, CustomUserDetailsService et les autorites
 * ROLE_ADMIN_&lt;team&gt; continuent de fonctionner a l'identique.
 * <p>
 * Aucun comptage de tentatives ici, contrairement au mot de passe : une assertion WebAuthn ne se
 * devine pas, il n'y a pas de bourrage d'identifiants a freiner. Le debit est limite en amont,
 * sur l'emission des options de connexion.
 */
@Service
public class PasskeyAuthenticationProvider implements AuthenticationProvider {

    private static final String MSG_FAILED = PasskeyAuthenticationService.MSG_FAILED;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> authentication) {
        return PasskeyAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {

        final PasskeyAuthenticationToken token = (PasskeyAuthenticationToken) auth;

        final String userId = String.valueOf(token.getPrincipal());

        final User user = userService.get(userId)
                .orElseThrow(() -> new BadCredentialsException(MSG_FAILED));

        if (user.isDeletion()) {
            throw new DisabledException("Ce compte est en cours de suppression.");
        }

        // authentification interactive : la graine heritee de la migration est remplacee
        final User refreshed = userService.ensureAuthTokenSeed(user);

        final OAuth2UserDetails principal = OAuth2UserDetails.create(refreshed);

        return PasskeyAuthenticationToken.authenticated(principal, token.getPasskeyId(), principal.getAuthorities());

    }

}
