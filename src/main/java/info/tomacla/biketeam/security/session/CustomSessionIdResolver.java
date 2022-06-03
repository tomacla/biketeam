package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class CustomSessionIdResolver implements HttpSessionIdResolver {

    private static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    private static final String WRITTEN_SESSION_ID_ATTR = CookieHttpSessionIdResolver.class.getName()
            .concat(".WRITTEN_SESSION_ID_ATTR");

    private final CookieSerializer cookieSerializer = new DefaultCookieSerializer();
    private final UrlService urlService;
    private final SSOService ssoService;

    @Autowired
    public CustomSessionIdResolver(UrlService urlService, SSOService ssoService) {
        this.urlService = urlService;
        this.ssoService = ssoService;
    }

    @PostConstruct
    public void init() {
        ((DefaultCookieSerializer) cookieSerializer).setDomainName(urlService.getCookieDomain());
    }

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {

        // try to resolve by sso param
        final String sso = getSSOParam(request);
        if (sso != null) {
            String ssoToken = URLDecoder.decode(sso, StandardCharsets.UTF_8);
            final Optional<String> authTokenFromSSOToken = ssoService.getSessionIdFromSSOToken(ssoToken);
            if (authTokenFromSSOToken.isPresent()) {
                return List.of(authTokenFromSSOToken.get());
            }
        }

        // try to resolve by http header
        final String xAuthHeader = getXAuthHeader(request);
        if (xAuthHeader != null) {
            return List.of(xAuthHeader);
        }

        // finaly try to resolve from cookie
        return this.cookieSerializer.readCookieValues(request);

    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        if (sessionId.equals(request.getAttribute(WRITTEN_SESSION_ID_ATTR))) {
            return;
        }
        request.setAttribute(WRITTEN_SESSION_ID_ATTR, sessionId);
        this.cookieSerializer.writeCookieValue(new CookieSerializer.CookieValue(request, response, sessionId));
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        this.cookieSerializer.writeCookieValue(new CookieSerializer.CookieValue(request, response, ""));
    }

    private String getSSOParam(HttpServletRequest request) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .query(request.getQueryString())
                .build();

        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        return queryParams.getFirst(SSOService.SSO_PARAM);
    }

    private String getXAuthHeader(HttpServletRequest request) {
        return request.getHeader(HEADER_X_AUTH_TOKEN);
    }

}
