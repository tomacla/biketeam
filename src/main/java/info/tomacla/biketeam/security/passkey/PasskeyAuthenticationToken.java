package info.tomacla.biketeam.security.passkey;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Jeton d'authentification par passkey.
 * <p>
 * Un type dedie est indispensable : {@link info.tomacla.biketeam.security.password.EmailPasswordAuthenticationProvider}
 * declare supporter {@code UsernamePasswordAuthenticationToken}, et reutiliser ce type ferait
 * passer chaque connexion par passkey dans le provider mot de passe, qui la rejetterait.
 * <p>
 * Le jeton non authentifie ne porte que l'identifiant du compte designe par l'assertion : la
 * verification cryptographique a deja eu lieu dans {@link PasskeyAuthenticationFilter}, qui est
 * le seul endroit ou la requete HTTP - donc le challenge de session - est disponible. Le provider
 * ne tranche que la question du compte : existe-t-il, est-il actif.
 */
public class PasskeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private final String passkeyId;

    private PasskeyAuthenticationToken(Object principal,
                                       String passkeyId,
                                       Collection<? extends GrantedAuthority> authorities,
                                       boolean authenticated) {
        super(authorities);
        this.principal = principal;
        this.passkeyId = passkeyId;
        super.setAuthenticated(authenticated);
    }

    public static PasskeyAuthenticationToken unauthenticated(String userId, String passkeyId) {
        return new PasskeyAuthenticationToken(userId, passkeyId, null, false);
    }

    public static PasskeyAuthenticationToken authenticated(Object principal,
                                                           String passkeyId,
                                                           Collection<? extends GrantedAuthority> authorities) {
        return new PasskeyAuthenticationToken(principal, passkeyId, authorities, true);
    }

    @Override
    public Object getCredentials() {
        // la preuve est la signature deja verifiee : rien a conserver, et surtout rien a
        // exposer dans la session ou dans les logs
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getPasskeyId() {
        return passkeyId;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Utiliser PasskeyAuthenticationToken.authenticated() pour obtenir un jeton authentifie");
        }
        super.setAuthenticated(false);
    }

}
