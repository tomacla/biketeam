package info.tomacla.biketeam.security.oauth2.provider;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.UserProfileImageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Connexion de transition. Supprimable avec la registration {@code strava} d'application.properties,
 * le bouton de login.ftlh et le bootstrap admin.strava-id.
 * <p>
 * Ce handler ne pose JAMAIS d'email verifie et n'appelle JAMAIS
 * {@link info.tomacla.biketeam.security.oauth2.link.AccountLinkService} : un compte cree par
 * Strava reste "incomplet" par construction, et sera donc invite a se doter d'une vraie identite
 * de connexion (email + mot de passe, ou Google/Facebook).
 *
 * @deprecated la connexion Strava sera supprimee. Aucune nouvelle fonctionnalite ne doit en dependre.
 */
@Deprecated
@Service
public class StravaProviderHandler implements OAuth2ProviderHandler {

    private static final Logger log = LoggerFactory.getLogger(StravaProviderHandler.class);

    private final UserService userService;
    private final BrokerService brokerService;

    @Autowired
    public StravaProviderHandler(UserService userService, BrokerService brokerService) {
        this.userService = userService;
        this.brokerService = brokerService;
    }

    @Override
    public String registrationId() {
        return "strava";
    }

    @Override
    public User resolve(DefaultOAuth2User user) {

        Map<String, Object> attributes = user.getAttributes();
        Long stravaId = Long.valueOf((Integer) attributes.get("id"));

        log.debug("Load user with strava id {}", stravaId);

        Optional<User> optionalUser = userService.getByStravaId(stravaId);
        User u;
        String profileImage = (String) attributes.get("profile_medium");
        if (optionalUser.isEmpty()) {

            log.debug("Register new user with strava id {}", stravaId);

            u = new User();
            u.setFirstName((String) attributes.get("firstname"));
            u.setLastName((String) attributes.get("lastname"));
            u.setStravaId(stravaId);
            u.setStravaUserName((String) attributes.get("username"));
            u.setCity((String) attributes.get("city"));

        } else {

            log.debug("Updating existing user with strava id {}", stravaId);

            u = optionalUser.get();
            u.setStravaUserName((String) attributes.get("username"));
            u.setFirstName((String) attributes.get("firstname"));
            u.setLastName((String) attributes.get("lastname"));
            u.setCity((String) attributes.get("city"));

        }

        u = userService.save(u);
        if (!Strings.isBlank(profileImage)) {
            brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE,
                    UserProfileImageDTO.valueOf(u.getId(), profileImage));
        }

        return u;

    }

}
