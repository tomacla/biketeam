package info.tomacla.biketeam.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String DEFAULT = "/";
    public static final String SEPARATOR = ",";

    @Autowired
    private SSOService ssoService;

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .query(request.getQueryString())
                .build();

        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String stateEncoded = queryParams.getFirst("state");
        if (stateEncoded == null) {
            return DEFAULT;
        }
        String stateDecoded = URLDecoder.decode(stateEncoded, StandardCharsets.UTF_8);
        String[] split = stateDecoded.split(SEPARATOR);
        if (split.length == 2) {
            String referer = split[1];
            final HttpSession session = request.getSession(false);
            if (session == null) {
                return referer;
            }
            if (referer.indexOf('?') != -1) {
                return referer + "&sso=" + ssoService.getSSOToken(session.getId());
            }
            return referer + "?sso=" + ssoService.getSSOToken(session.getId());
        }
        return super.determineTargetUrl(request, response);
    }

}