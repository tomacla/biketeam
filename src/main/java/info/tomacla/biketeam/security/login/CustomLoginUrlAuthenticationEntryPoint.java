package info.tomacla.biketeam.security.login;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final String LOGIN_FORM = "/login";

    public CustomLoginUrlAuthenticationEntryPoint() {
        super(LOGIN_FORM);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        if (isApi(request.getRequestURI())) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access Denied");
        } else {
            super.commence(request, response, authException);
        }

    }


    private boolean isApi(String uri) {
        return uri.startsWith("/api");
    }

}
