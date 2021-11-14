package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Oauth2AuthUserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(Oauth2AuthUserService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2User user = (DefaultOAuth2User) super.loadUser(userRequest);

        List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
        Map<String, Object> attributes = user.getAttributes();
        Long stravaId = Long.valueOf((Integer) attributes.get("id"));

        log.debug("Load user with strava id {}", stravaId);

        Optional<User> optionalUser = userService.getByStravaId(stravaId);
        User u;
        if (optionalUser.isEmpty()) {

            log.debug("Register new user with strava id {}", stravaId);

            u = new User(
                    false,
                    (String) attributes.get("firstname"),
                    (String) attributes.get("lastname"),
                    stravaId,
                    (String) attributes.get("username"),
                    (String) attributes.get("city"),
                    (String) attributes.get("profile_medium"),
                    null,
                    null
            );
            userService.save(u);

        } else {

            log.debug("Updating existing user with strava id {}", stravaId);

            u = optionalUser.get();
            u.setFirstName((String) attributes.get("firstname"));
            u.setLastName((String) attributes.get("lastname"));
            u.setCity((String) attributes.get("city"));
            u.setProfileImage((String) attributes.get("profile_medium"));
            userService.save(u);

        }

        return OAuth2UserDetails.create(u, teamService.getUserTeamsMember(u.getId()), teamService.getUserTeamsAdmin(u.getId()));

    }

}
