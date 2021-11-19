package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return OAuth2UserDetails.create(userService.get(s)
                .orElseThrow(() -> new UsernameNotFoundException("Logged user can not be found")));
    }

}
