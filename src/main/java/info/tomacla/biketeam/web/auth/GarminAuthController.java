package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.service.garmin.GarminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping
public class GarminAuthController {

    @Autowired
    private GarminAuthService garminAuthService;

    @RequestMapping(value = "/auth/garmin", method = RequestMethod.GET)
    public void fromGarmin(HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session,
                           @RequestParam("oauth_token") String oauthToken,
                           @RequestParam("oauth_verifier") String oauthVerifier) throws Exception {
        garminAuthService.auth(request, response, session, oauthToken, oauthVerifier);
    }

}
