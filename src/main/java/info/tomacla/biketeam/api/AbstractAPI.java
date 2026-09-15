package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    /**
     * Tout type d'authentification est accepte (OAuth2, remember-me, form login) des lors que le
     * principal est un OAuth2UserDetails : c'est le seul principal pose par l'application.
     */
    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof Authentication a && a.getPrincipal() instanceof OAuth2UserDetails ud) {
            return userService.get(ud.getUsername());
        }
        return Optional.empty();
    }

}
