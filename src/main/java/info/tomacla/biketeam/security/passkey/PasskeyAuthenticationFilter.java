package info.tomacla.biketeam.security.passkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.user.UserPasskey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

/**
 * Traitement de {@code POST /login/webauthn}.
 * <p>
 * Le filtre porte la partie protocolaire : lecture du corps JSON et verification de l'assertion,
 * qui a besoin de la requete HTTP pour retrouver le challenge pose en session. Il delegue ensuite
 * a l'AuthenticationManager la seule question qui releve du metier : le compte designe
 * est-il utilisable.
 * <p>
 * Passer par {@link AbstractAuthenticationProcessingFilter} plutot que par un controleur MVC n'est
 * pas cosmetique : c'est lui qui applique la protection contre la fixation de session, enregistre
 * le SecurityContext dans le depot (obligatoire depuis Spring Security 6) et declenche l'emission
 * du cookie remember-me, exactement comme pour la connexion par mot de passe.
 */
public class PasskeyAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String LOGIN_URL = "/login/webauthn";

    private final PasskeyAuthenticationService passkeyAuthenticationService;

    private final ObjectMapper objectMapper;

    public PasskeyAuthenticationFilter(AuthenticationManager authenticationManager,
                                       PasskeyAuthenticationService passkeyAuthenticationService,
                                       ObjectMapper objectMapper) {
        super(AntPathRequestMatcher.antMatcher(HttpMethod.POST, LOGIN_URL), authenticationManager);
        this.passkeyAuthenticationService = passkeyAuthenticationService;
        this.objectMapper = objectMapper;
        setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        final PasskeyAuthenticationBody body;
        try {
            body = objectMapper.readValue(request.getInputStream(), PasskeyAuthenticationBody.class);
        } catch (IOException e) {
            throw new BadCredentialsException(PasskeyAuthenticationService.MSG_FAILED);
        }

        final UserPasskey passkey;
        try {
            passkey = passkeyAuthenticationService.verify(request, body);
        } catch (PasskeyException e) {
            // le message est deja redige pour l'utilisateur ; la cause technique est journalisee
            // par le service, elle n'a pas a remonter jusqu'au navigateur
            throw new BadCredentialsException(e.getMessage());
        }

        return getAuthenticationManager().authenticate(
                PasskeyAuthenticationToken.unauthenticated(passkey.getUserId(), passkey.getId()));

    }

}
