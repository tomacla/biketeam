package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.CurrentUserResolver;
import info.tomacla.biketeam.security.oauth2.link.AccountLinkService;
import info.tomacla.biketeam.security.oauth2.link.OAuth2LinkIntentStore;
import info.tomacla.biketeam.security.oauth2.provider.FacebookProviderHandler;
import info.tomacla.biketeam.security.oauth2.provider.GoogleProviderHandler;
import info.tomacla.biketeam.security.oauth2.provider.StravaProviderHandler;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.amqp.BrokerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Oauth2AuthUserServiceTest {

    /**
     * Le dispatcheur est construit avec les trois handlers, et un AccountLinkService reel dont les
     * collaborateurs sont mockes : par defaut CurrentUserResolver ne renvoie aucun utilisateur
     * courant, ce qui correspond au cas nominal d'une connexion (aucune liaison).
     */
    private Oauth2AuthUserService newService(UserService userService, BrokerService brokerService) {

        final CurrentUserResolver currentUserResolver = Mockito.mock(CurrentUserResolver.class);
        final OAuth2LinkIntentStore linkIntentStore = Mockito.mock(OAuth2LinkIntentStore.class);
        final AccountLinkService accountLinkService =
                new AccountLinkService(userService, currentUserResolver, linkIntentStore);

        return new Oauth2AuthUserService(userService, List.of(
                new StravaProviderHandler(userService, brokerService),
                new GoogleProviderHandler(userService, brokerService, accountLinkService),
                new FacebookProviderHandler(userService, accountLinkService)
        ));

    }

    @Test
    public void testStravaUnknown() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByStravaId(10002L)).thenReturn(Optional.empty());
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 10002);
        attributes.put("firstname", "foo");
        attributes.put("lastname", "bar");
        attributes.put("username", "foobar");
        attributes.put("city", "paris");
        attributes.put("profile_medium", "https://profile");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "strava");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals(10002L, savedUser.getStravaId());
        assertEquals("foo", savedUser.getFirstName());
        assertEquals("bar", savedUser.getLastName());
        assertEquals("foobar", savedUser.getStravaUserName());
        assertEquals("paris", savedUser.getCity());

        // un compte cree par Strava reste incomplet : ni email, ni email verifie
        assertEquals(null, savedUser.getEmail());
        assertEquals(false, savedUser.isEmailVerified());

        Mockito.verify(userService).ensureAuthTokenSeed(Mockito.any());

        Mockito.verify(brokerService).sendToBroker(Mockito.eq(Exchanges.TASK), Mockito.eq(RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE),
                Mockito.any());

    }

    @Test
    public void testStravaExisting() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        User u = new User();
        u.setStravaId(10002L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByStravaId(10002L)).thenReturn(Optional.of(u));
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 10002);
        attributes.put("firstname", "foo");
        attributes.put("lastname", "bar");
        attributes.put("username", "foobar");
        attributes.put("city", "paris");
        attributes.put("profile_medium", "https://profile");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "strava");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals(u.getId(), savedUser.getId());
        assertEquals(10002L, savedUser.getStravaId());
        assertEquals("foo", savedUser.getFirstName());
        assertEquals("bar", savedUser.getLastName());
        assertEquals("foobar", savedUser.getStravaUserName());
        assertEquals("paris", savedUser.getCity());

        Mockito.verify(brokerService).sendToBroker(Mockito.eq(Exchanges.TASK), Mockito.eq(RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE),
                Mockito.any());

    }

    @Test
    public void testFacebookUnknown() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByFacebookId("10002")).thenReturn(Optional.empty());
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "10002");
        attributes.put("name", "foo bar");
        attributes.put("email", "foo@bar.com");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "facebook");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getFacebookId());
        assertEquals("foo", savedUser.getFirstName());
        assertEquals("bar", savedUser.getLastName());
        assertEquals("foo@bar.com", savedUser.getEmail());
        // facebook ne transmet l'adresse que si la permission a ete accordee : elle vaut preuve
        assertEquals(true, savedUser.isEmailVerified());

    }

    @Test
    public void testFacebookExisting() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        User u = new User();
        u.setFacebookId("10002");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByFacebookId("10002")).thenReturn(Optional.of(u));
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "10002");
        attributes.put("name", "foo bar");
        attributes.put("email", "foo@bar.com");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "facebook");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getFacebookId());
        assertEquals("foo@bar.com", savedUser.getEmail());

    }

    /**
     * Durcissement : une adresse deja utilisee par un autre compte n'est jamais reprise.
     * L'identite OAuth2 est liee, l'adresse reste au compte qui la possede.
     */
    @Test
    public void testFacebookEmailOwnedBySomeoneElse() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByFacebookId("10002")).thenReturn(Optional.empty());
        // l'adresse appartient a un compte dont l'email n'est pas verifie : aucun repli possible
        Mockito.when(userService.getByEmail("foo@bar.com")).thenReturn(Optional.empty());
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(false);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "10002");
        attributes.put("name", "foo bar");
        attributes.put("email", "foo@bar.com");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        oauth2AuthUserService.loadUser(user, "facebook");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getFacebookId());
        assertEquals(null, savedUser.getEmail());

    }

    @Test
    public void testGoogleUnknown() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByGoogleId("10002")).thenReturn(Optional.empty());
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "10002");
        attributes.put("given_name", "foo");
        attributes.put("family_name", "bar");
        attributes.put("email", "foo@bar.com");
        attributes.put("email_verified", Boolean.TRUE);
        attributes.put("picture", "picture");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "google");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getGoogleId());
        assertEquals("foo", savedUser.getFirstName());
        assertEquals("bar", savedUser.getLastName());
        assertEquals("foo@bar.com", savedUser.getEmail());
        assertEquals(true, savedUser.isEmailVerified());

        Mockito.verify(brokerService).sendToBroker(Mockito.eq(Exchanges.TASK), Mockito.eq(RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE),
                Mockito.any());

    }

    /**
     * Durcissement : sans email_verified, l'adresse fournie par Google n'est pas prouvee. Elle est
     * enregistree mais NON verifiee, et ne donne acces a aucun compte existant.
     */
    @Test
    public void testGoogleUnknownWithUnverifiedEmail() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByGoogleId("10002")).thenReturn(Optional.empty());
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "10002");
        attributes.put("given_name", "foo");
        attributes.put("family_name", "bar");
        attributes.put("email", "foo@bar.com");
        attributes.put("picture", "picture");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        oauth2AuthUserService.loadUser(user, "google");

        // aucun repli par email : le compte existant n'est jamais recherche sur une adresse non prouvee
        Mockito.verify(userService, Mockito.never()).getByEmail(Mockito.anyString());

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getGoogleId());
        assertEquals("foo@bar.com", savedUser.getEmail());
        assertEquals(false, savedUser.isEmailVerified());

    }

    @Test
    public void testGoogleExisting() {

        UserService userService = Mockito.mock(UserService.class);
        BrokerService brokerService = Mockito.mock(BrokerService.class);

        User u = new User();
        u.setFacebookId("10002");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByGoogleId("10002")).thenReturn(Optional.of(u));
        Mockito.when(userService.isEmailAvailable(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(userService.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        final Oauth2AuthUserService oauth2AuthUserService = newService(userService, brokerService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "10002");
        attributes.put("given_name", "foo");
        attributes.put("family_name", "bar");
        attributes.put("email", "foo@bar.com");
        attributes.put("email_verified", Boolean.TRUE);
        attributes.put("picture", "picture");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "google");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getGoogleId());
        assertEquals("foo@bar.com", savedUser.getEmail());

        Mockito.verify(brokerService).sendToBroker(Mockito.eq(Exchanges.TASK), Mockito.eq(RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE),
                Mockito.any());

    }

}
