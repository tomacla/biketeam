package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class OAuth2StateWriter extends Base64StringKeyGenerator {

    private OAuth2StateHandler stateHandler;

    @Autowired
    public OAuth2StateWriter(UrlService urlService, TeamService teamService) {
        this.stateHandler = new OAuth2StateHandler(urlService, teamService);
    }

    @Override
    public String generateKey() {
        return generateKey(getCurrentHttpRequest());
    }

    public String generateKey(HttpServletRequest currentHttpRequest) {
        return stateHandler.generateState(currentHttpRequest);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

}