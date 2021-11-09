package info.tomacla.biketeam.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CheckTeamAdminService {

    public boolean authorize(Authentication authentication, String teamId) {
        return authentication.getAuthorities().contains(Authorities.teamAdmin(teamId));
    }

}
