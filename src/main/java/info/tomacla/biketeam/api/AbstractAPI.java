package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

public abstract class AbstractAPI {

    @Autowired
    protected TeamService teamService;

    @Autowired
    private UserService userService;

    protected Team checkTeam(String teamId) {
        return teamService.get(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team " + teamId));
    }

    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken wrapperPrincipal = (OAuth2AuthenticationToken) principal;
            OAuth2UserDetails oauthprincipal = (OAuth2UserDetails) wrapperPrincipal.getPrincipal();
            return userService.get(oauthprincipal.getUsername());
        }
        if (principal instanceof RememberMeAuthenticationToken) {
            RememberMeAuthenticationToken wrapperPrincipal = (RememberMeAuthenticationToken) principal;
            OAuth2UserDetails oauthprincipal = (OAuth2UserDetails) wrapperPrincipal.getPrincipal();
            return userService.get(oauthprincipal.getUsername());
        }
        return Optional.empty();
    }

}
