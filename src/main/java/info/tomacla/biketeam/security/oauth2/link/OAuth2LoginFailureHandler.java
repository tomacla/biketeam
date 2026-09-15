package info.tomacla.biketeam.security.oauth2.link;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Un conflit de liaison n'est pas une erreur d'authentification a afficher sur /login : il ouvre
 * l'ecran de confirmation de fusion, ou l'utilisateur decide explicitement du sort des deux comptes.
 * <p>
 * Toute autre erreur conserve le comportement par defaut (/login?error).
 */
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public OAuth2LoginFailureHandler() {
        super("/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof OAuth2AuthenticationException oauth2Exception
                && oauth2Exception.getError() != null
                && AccountLinkService.ACCOUNT_LINK_CONFLICT.equals(oauth2Exception.getError().getErrorCode())) {

            // DefaultRedirectStrategy prefixe deja le contexte de deploiement : le rajouter ici
            // produirait /<context>/<context>/account/link-conflict
            getRedirectStrategy().sendRedirect(request, response, "/account/link-conflict");
            return;

        }

        super.onAuthenticationFailure(request, response, exception);

    }

}
