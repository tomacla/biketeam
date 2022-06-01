package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.security.session.SSOService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private SSOService ssoService;

    private OAuth2StateHandler stateHandler;

    @Autowired
    public OAuth2FailureHandler(SSOService ssoService, UrlService urlService, TeamService teamService) {
        this.ssoService = ssoService;
        this.stateHandler = new OAuth2StateHandler(urlService, teamService);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String stateDecoded = getStateDecoded(request);
        String referer = stateHandler.decodeState(stateDecoded);

        if (referer != null) {

            if (referer.indexOf('?') != -1) {
                referer += "&error=AUTHENTICATION_ERROR";
            }
            referer += "?error=AUTHENTICATION_ERROR";

            getRedirectStrategy().sendRedirect(request, response, referer);


        } else {
            super.onAuthenticationFailure(request, response, exception);
        }

    }


    public String getStateDecoded(HttpServletRequest request) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .query(request.getQueryString())
                .build();

        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String stateEncoded = queryParams.getFirst("state");

        if (stateEncoded == null) {
            return null;
        }

        return URLDecoder.decode(stateEncoded, StandardCharsets.UTF_8);
    }

}
