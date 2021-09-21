package info.tomacla.biketeam.security;

import info.tomacla.biketeam.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Order(SessionRepositoryFilter.DEFAULT_ORDER - 1)
public class SSOTokenFilter extends OncePerRequestFilter {

    @Autowired
    private SSOService ssoService;

    @Autowired
    private UrlService urlService;

    private CookieSerializer cookieSerializer = new DefaultCookieSerializer();

    @PostConstruct
    public void init() {
        ((DefaultCookieSerializer) cookieSerializer).setDomainName(urlService.getCookieDomain());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .query(request.getQueryString())
                .build();

        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String sso = queryParams.getFirst("sso");
        if (sso != null) {
            String ssoToken = URLDecoder.decode(sso, StandardCharsets.UTF_8);
            final Optional<String> authTokenFromSSOToken = ssoService.getAuthTokenFromSSOToken(ssoToken);
            if (authTokenFromSSOToken.isPresent()) {
                String sessionId = authTokenFromSSOToken.get();
                cookieSerializer.writeCookieValue(new CookieSerializer.CookieValue(request, response, sessionId));
            }
        }

        filterChain.doFilter(request, response);

    }
}
