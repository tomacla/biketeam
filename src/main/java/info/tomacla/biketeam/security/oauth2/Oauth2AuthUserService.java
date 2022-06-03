package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class Oauth2AuthUserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(Oauth2AuthUserService.class);

    private final UserService userService;

    @Autowired
    public Oauth2AuthUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2User user = (DefaultOAuth2User) super.loadUser(userRequest);
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return loadUser(user, registrationId);
    }

    protected OAuth2User loadUser(DefaultOAuth2User user, String registrationId) {
        switch (registrationId) {
            case "facebook":
                return handleFacebookProvider(user);
            case "google":
                return handleGoogleProvider(user);
            case "strava":
                return handleStravaProvider(user);
        }
        return user;
    }

    private OAuth2UserDetails handleStravaProvider(DefaultOAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        Long stravaId = Long.valueOf((Integer) attributes.get("id"));

        log.debug("Load user with strava id {}", stravaId);

        Optional<User> optionalUser = userService.getByStravaId(stravaId);
        User u;
        if (optionalUser.isEmpty()) {

            log.debug("Register new user with strava id {}", stravaId);

            u = new User();
            u.setFirstName((String) attributes.get("firstname"));
            u.setLastName((String) attributes.get("lastname"));
            u.setStravaId(stravaId);
            u.setStravaUserName((String) attributes.get("username"));
            u.setCity((String) attributes.get("city"));
            u.setProfileImage((String) attributes.get("profile_medium"));

        } else {

            log.debug("Updating existing user with strava id {}", stravaId);

            u = optionalUser.get();
            u.setStravaUserName((String) attributes.get("username"));
            u.setFirstName((String) attributes.get("firstname"));
            u.setLastName((String) attributes.get("lastname"));
            u.setCity((String) attributes.get("city"));
            u.setProfileImage((String) attributes.get("profile_medium"));

        }

        userService.save(u);

        return OAuth2UserDetails.create(u);
    }


    private OAuth2UserDetails handleGoogleProvider(DefaultOAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        String googleId = (String) attributes.get("sub");

        log.debug("Load user with google id {}", googleId);

        Optional<User> optionalUser = userService.getByGoogleId(googleId);
        if (optionalUser.isEmpty() && attributes.get("email") != null) {
            optionalUser = userService.getByEmail((String) attributes.get("email"));
        }

        User u;
        if (optionalUser.isEmpty()) {

            log.debug("Register new user with google id {}", googleId);

            u = new User();
            u.setFirstName((String) attributes.get("given_name"));
            u.setLastName((String) attributes.get("family_name"));
            u.setProfileImage((String) attributes.get("picture"));
            u.setGoogleId(googleId);

            if (attributes.get("email") != null) {
                u.setEmail((String) attributes.get("email"));
            }

        } else {
            u = optionalUser.get();

            if (attributes.get("email") != null) {
                u.setEmail((String) attributes.get("email"));
            }

            u.setGoogleId(googleId);

        }

        userService.save(u);

        return OAuth2UserDetails.create(u);
    }

    private OAuth2UserDetails handleFacebookProvider(DefaultOAuth2User user) {
        Map<String, Object> attributes = user.getAttributes();
        String facebookId = (String) attributes.get("id");

        log.debug("Load user with facebook id {}", facebookId);

        Optional<User> optionalUser = userService.getByFacebookId(facebookId);
        if (optionalUser.isEmpty() && attributes.get("email") != null) {
            optionalUser = userService.getByEmail((String) attributes.get("email"));
        }

        User u;
        if (optionalUser.isEmpty()) {

            log.debug("Register new user with facebook id {}", facebookId);

            String fullName = (String) attributes.get("name");
            final String[] nameParts = fullName.split(" ");
            String firstName = fullName;
            String lastName = "";
            if (nameParts.length > 1) {
                firstName = nameParts[0];
                lastName = nameParts[1];
            }

            u = new User();
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setFacebookId(facebookId);

            if (attributes.get("email") != null) {
                u.setEmail((String) attributes.get("email"));
            }

        } else {
            u = optionalUser.get();

            if (attributes.get("email") != null) {
                u.setEmail((String) attributes.get("email"));
            }

            u.setFacebookId(facebookId);

        }

        userService.save(u);

        return OAuth2UserDetails.create(u);
    }

}
