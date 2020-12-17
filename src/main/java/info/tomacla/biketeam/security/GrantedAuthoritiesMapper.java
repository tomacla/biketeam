package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GrantedAuthoritiesMapper implements org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {

        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            if (authority instanceof OAuth2UserAuthority) {
                OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;

                Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                Long stravaId = Long.valueOf((Integer) userAttributes.get("id"));

                Optional<User> optionalUser = userRepository.findByStravaId(stravaId);
                if (optionalUser.isEmpty()) {
                    User u = new User(
                            false,
                            (String) userAttributes.get("firstname"),
                            (String) userAttributes.get("lastname"),
                            stravaId,
                            (String) userAttributes.get("username"),
                            (String) userAttributes.get("city"),
                            (String) userAttributes.get("profile_medium")
                    );
                    userRepository.save(u);

                } else {

                    User u = optionalUser.get();
                    u.setFirstName((String) userAttributes.get("firstname"));
                    u.setLastName((String) userAttributes.get("lastname"));
                    u.setCity((String) userAttributes.get("city"));
                    u.setProfileImage((String) userAttributes.get("profile_medium"));
                    userRepository.save(u);

                    if (u.isAdmin()) {
                        mappedAuthorities.add(AdminAuthority.get());
                    }

                }

            }
        });

        return mappedAuthorities;

    }

}
