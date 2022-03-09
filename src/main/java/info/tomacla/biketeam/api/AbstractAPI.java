package info.tomacla.biketeam.api;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractAPI {

    @Autowired
    protected TeamService teamService;

    protected Team checkTeam(String teamId) {
        return teamService.get(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team " + teamId));
    }

}
