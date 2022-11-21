package info.tomacla.biketeam.security.login;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        if (isApi(request.getRequestURI())) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
        } else {
            request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            request.getRequestDispatcher("/?error=Acc%C3%A8s%20interdit").forward(request, response);
        }


    }

    private boolean isApi(String requestUri) {
        return requestUri.startsWith("/api");
    }

}
