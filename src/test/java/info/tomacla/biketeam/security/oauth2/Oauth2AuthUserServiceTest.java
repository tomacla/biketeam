package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Oauth2AuthUserServiceTest {

    @Test
    public void testStravaUnknown() {

        UserService userService = Mockito.mock(UserService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByStravaId(10002L)).thenReturn(Optional.empty());

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

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
        assertEquals("https://profile", savedUser.getProfileImage());

    }

    @Test
    public void testStravaExisting() {

        UserService userService = Mockito.mock(UserService.class);

        User u = new User();
        u.setStravaId(10002L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByStravaId(10002L)).thenReturn(Optional.of(u));

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

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
        assertEquals("https://profile", savedUser.getProfileImage());

    }

    @Test
    public void testFacebookUnknown() {

        UserService userService = Mockito.mock(UserService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByFacebookId("10002")).thenReturn(Optional.empty());

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

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

    }

    @Test
    public void testFacebookExisting() {

        UserService userService = Mockito.mock(UserService.class);

        User u = new User();
        u.setFacebookId("10002");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByFacebookId("10002")).thenReturn(Optional.of(u));

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

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

    @Test
    public void testGoogleUnknown() {

        UserService userService = Mockito.mock(UserService.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByGoogleId("10002")).thenReturn(Optional.empty());

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "10002");
        attributes.put("given_name", "foo");
        attributes.put("family_name", "bar");
        attributes.put("email", "foo@bar.com");
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
        assertEquals("picture", savedUser.getProfileImage());

    }

    @Test
    public void testGoogleExisting() {

        UserService userService = Mockito.mock(UserService.class);

        User u = new User();
        u.setFacebookId("10002");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userService.getByGoogleId("10002")).thenReturn(Optional.of(u));

        final Oauth2AuthUserService oauth2AuthUserService = new Oauth2AuthUserService(userService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "10002");
        attributes.put("given_name", "foo");
        attributes.put("family_name", "bar");
        attributes.put("email", "foo@bar.com");
        attributes.put("picture", "picture");

        final DefaultOAuth2User user = Mockito.mock(DefaultOAuth2User.class);
        Mockito.when(user.getAttributes()).thenReturn(attributes);

        final OAuth2User finalUser = oauth2AuthUserService.loadUser(user, "google");

        Mockito.verify(userService).save(userCaptor.capture());
        final User savedUser = userCaptor.getValue();
        assertEquals("10002", savedUser.getGoogleId());
        assertEquals("foo@bar.com", savedUser.getEmail());

    }

}
