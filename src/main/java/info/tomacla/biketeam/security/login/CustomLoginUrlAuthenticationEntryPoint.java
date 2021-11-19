package info.tomacla.biketeam.security.login;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public CustomLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        String redirectUrl = super.buildRedirectUrlToLoginPage(request, response, authException);
        final String referer = request.getRequestURI();
        if (referer != null) {
            if (redirectUrl.indexOf('?') != -1) {
                return redirectUrl + "&requestUri=" + referer;
            }
            return redirectUrl + "?requestUri=" + referer;
        }
        return redirectUrl;
    }
}
