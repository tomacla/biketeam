package info.tomacla.biketeam.security.passkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Echec de connexion par passkey : reponse JSON, pour la meme raison que le handler de succes.
 * <p>
 * Le message renvoye est uniformement celui du service : il ne distingue jamais "passkey inconnue"
 * de "signature invalide", faute de quoi l'endpoint deviendrait un oracle permettant d'enumerer
 * les credentials enregistres.
 */
public class PasskeyLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public PasskeyLoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("error", exception.getMessage()));

    }

}
