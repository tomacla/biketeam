package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OAuth2UserDetailsTest {

    @Test
    public void test() {

        Team t1 = new Team();
        t1.setId("t1");

        Team t2 = new Team();
        t2.setId("t2");

        User u = new User();
        u.setId("id");
        u.setFacebookId("facebookId");
        u.setStravaUserName("stravaUserName");
        u.setCity("city");
        u.setStravaId(10002L);
        u.setAdmin(true);
        u.setLastName("lastname");
        u.setFirstName("firstname");
        u.setGoogleId("google");
        u.setEmail("email");

        u.setRoles(Set.of(new UserRole(t1, u, Role.ADMIN),
                new UserRole(t2, u, Role.MEMBER)));

        final OAuth2UserDetails finalUser = OAuth2UserDetails.create(u);

        assertEquals(finalUser.getAttributes().get("id"), "id");
        assertEquals(finalUser.getAttributes().get("facebookId"), "facebookId");
        assertEquals(finalUser.getAttributes().get("stravaUserName"), "stravaUserName");
        assertEquals(finalUser.getAttributes().get("city"), "city");
        assertEquals(finalUser.getAttributes().get("admin"), true);
        assertEquals(finalUser.getAttributes().get("lastName"), "lastname");
        assertEquals(finalUser.getAttributes().get("firstName"), "firstname");
        assertEquals(finalUser.getAttributes().get("stravaId"), 10002L);
        assertEquals(finalUser.getAttributes().get("googleId"), "google");


        assertEquals(5, finalUser.getAuthorities().size());
        assertTrue(finalUser.getAuthorities().contains(Authorities.admin()));
        assertTrue(finalUser.getAuthorities().contains(Authorities.user()));
        assertTrue(finalUser.getAuthorities().contains(Authorities.teamAdmin("t1")));
        assertTrue(finalUser.getAuthorities().contains(Authorities.teamUser("t1")));
        assertTrue(finalUser.getAuthorities().contains(Authorities.teamUser("t2")));


    }

    @Test
    public void testNotAdmin() {

        Team t1 = new Team();
        t1.setId("t1");

        Team t2 = new Team();
        t2.setId("t2");

        User u = new User();
        u.setId("id");
        u.setAdmin(false);

        u.setRoles(Set.of(new UserRole(t1, u, Role.MEMBER),
                new UserRole(t2, u, Role.MEMBER)));

        final OAuth2UserDetails finalUser = OAuth2UserDetails.create(u);

        assertEquals(finalUser.getAttributes().get("id"), "id");
        assertEquals(finalUser.getAttributes().get("admin"), false);


        assertEquals(3, finalUser.getAuthorities().size());
        assertTrue(finalUser.getAuthorities().contains(Authorities.user()));
        assertTrue(finalUser.getAuthorities().contains(Authorities.teamUser("t1")));
        assertTrue(finalUser.getAuthorities().contains(Authorities.teamUser("t2")));


    }
}
