package info.tomacla.biketeam.security.login;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.*;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLoginUrlAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String LOGIN_FORM = "/login";

    private PortResolver portResolver = new PortResolverImpl();

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        if (isApi(request.getRequestURI())) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access Denied");
        } else {
            String redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
            String referer = request.getRequestURI();
            if (referer != null) {
                if (redirectUrl.indexOf('?') != -1) {
                    redirectUrl += "&requestUri=" + referer;
                }
                redirectUrl += "?requestUri=" + referer;
            }
            this.redirectStrategy.sendRedirect(request, response, redirectUrl);
        }

    }

    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        String redirectUrl = defaultRedirectUrl(request, response, authException);
        final String referer = request.getRequestURI();
        if (referer != null) {
            if (redirectUrl.indexOf('?') != -1) {
                return redirectUrl + "&requestUri=" + referer;
            }
            return redirectUrl + "?requestUri=" + referer;
        }
        return redirectUrl;
    }

    private boolean isApi(String uri) {
        return uri.startsWith("/api");
    }

    protected String defaultRedirectUrl(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        if (UrlUtils.isAbsoluteUrl(LOGIN_FORM)) {
            return LOGIN_FORM;
        }
        int serverPort = this.portResolver.getServerPort(request);
        String scheme = request.getScheme();
        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
        urlBuilder.setScheme(scheme);
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(serverPort);
        urlBuilder.setContextPath(request.getContextPath());
        urlBuilder.setPathInfo(LOGIN_FORM);
        return urlBuilder.getUrl();
    }


}
