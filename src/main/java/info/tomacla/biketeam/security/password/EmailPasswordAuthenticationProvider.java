package info.tomacla.biketeam.security.password;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Authentification par email et mot de passe.
 * <p>
 * Le formulaire recoit un EMAIL, mais le principal pose reste un {@link OAuth2UserDetails} dont
 * getUsername() est l'IDENTIFIANT utilisateur : le cookie remember-me, CustomUserDetailsService
 * et les autorites ROLE_ADMIN_&lt;team&gt; continuent ainsi de fonctionner a l'identique.
 * C'est la raison pour laquelle DaoAuthenticationProvider n'est pas utilise.
 */
@Service
public class EmailPasswordAuthenticationProvider implements AuthenticationProvider {

    private static final String MSG_BAD_CREDENTIALS = "Email ou mot de passe incorrect.";

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * Hash factice servant a egaliser le temps de reponse entre un compte inexistant
     * et un mot de passe faux : sans lui, le temps de reponse revele les comptes existants.
     */
    private String dummyHash;

    @PostConstruct
    public void init() {
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {

        final String email = auth.getName() == null ? "" : auth.getName().trim().toLowerCase();
        final String raw = String.valueOf(auth.getCredentials());

        // l'origine de la requete est prise en compte dans le comptage : un verrou porte par la
        // seule adresse email permettrait a un tiers d'exclure indefiniment un utilisateur connu
        final String ip = auth.getDetails() instanceof WebAuthenticationDetails details
                ? details.getRemoteAddress() : null;

        loginAttemptService.assertNotLocked(email, ip);

        Optional<User> optional = userService.getByEmail(email);
        User user = optional.orElse(null);

        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(raw, user.getPasswordHash())) {
            if (user == null || user.getPasswordHash() == null) {
                passwordEncoder.matches(raw, dummyHash);
            }
            loginAttemptService.recordFailure(email, ip);
            throw new BadCredentialsException(MSG_BAD_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(user.getId(), user.getEmail());
        }

        loginAttemptService.recordSuccess(email, ip);

        // authentification interactive : la graine heritee de la migration est remplacee
        user = userService.ensureAuthTokenSeed(user);

        OAuth2UserDetails principal = OAuth2UserDetails.create(user);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());

    }

}
