package info.tomacla.biketeam.security.passkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Succes de connexion par passkey.
 * <p>
 * La requete vient de {@code fetch()} : une redirection 302 serait suivie de maniere transparente
 * par le navigateur et le script recevrait du HTML au lieu d'une reponse exploitable. On renvoie
 * donc l'URL cible en JSON, a charge du script de naviguer. La cible reste celle qu'attendrait
 * SavedRequestAwareAuthenticationSuccessHandler : la page demandee avant la redirection vers
 * /login, ou la racine.
 */
public class PasskeyLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final String defaultTargetUrl;

    public PasskeyLoginSuccessHandler(ObjectMapper objectMapper, String defaultTargetUrl) {
        this.objectMapper = objectMapper;
        this.defaultTargetUrl = defaultTargetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        final SavedRequest savedRequest = requestCache.getRequest(request, response);
        final String target = savedRequest == null ? defaultTargetUrl : savedRequest.getRedirectUrl();

        requestCache.removeRequest(request, response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("redirect", target));

    }

}
