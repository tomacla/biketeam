package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.security.session.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String DEFAULT = "/";
    public static final String SEPARATOR = ",";


    private SSOService ssoService;

    @Autowired
    public OAuth2SuccessHandler(SSOService ssoService) {
        this.ssoService = ssoService;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {

        String stateDecoded = getStateDecoded(request);
        if (stateDecoded == null) {
            return DEFAULT;
        }
        String[] split = stateDecoded.split(SEPARATOR);
        if (split.length == 2) {
            String referer = split[1];
            final HttpSession session = request.getSession(false);
            if (session == null) {
                return referer;
            }

            final String sessionId = session.getId();
            final String rememberMe = getRememberMe(request);

            if (referer.indexOf('?') != -1) {
                return referer + "&sso=" + ssoService.getSSOToken(sessionId, rememberMe);
            }
            return referer + "?sso=" + ssoService.getSSOToken(sessionId, rememberMe);
        }
        return super.determineTargetUrl(request, response);
    }

    private String getStateDecoded(HttpServletRequest request) {
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

    private String getRememberMe(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("remember-me")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
