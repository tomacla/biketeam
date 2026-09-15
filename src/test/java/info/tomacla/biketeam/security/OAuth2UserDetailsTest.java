package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    /**
     * Invariant verrouillant : la valeur presentee au login (getPassword()) et celle relue par
     * CustomUserDetailsService lors d'un auto-login remember-me DOIVENT etre identiques, sinon
     * 100% des cookies remember-me seraient invalides des la reconnexion. getUsername() doit
     * rester l'identifiant utilisateur (et non l'email) : c'est ce qui permet au login par email
     * de continuer a alimenter le meme cookie remember-me que les connexions OAuth2.
     */
    @Test
    public void testRememberMeInvariants() {

        User u = new User();
        u.setId("id1");
        u.setAuthTokenSeed("seed-abc");

        OAuth2UserDetails details = OAuth2UserDetails.create(u);

        assertEquals(u.getId(), details.getUsername());
        assertEquals(u.getAuthTokenSeed(), details.getPassword());
        assertNotEquals(u.getId(), details.getPassword());

    }

    @Test
    public void testGetPasswordIsAuthTokenSeedNotUserId() {

        User u = new User();
        u.setId("id1");
        // graine heritee de la migration (valeur = id) : cas legacy, pas encore tournee
        u.setAuthTokenSeed(u.getId());

        OAuth2UserDetails details = OAuth2UserDetails.create(u);
        assertEquals("id1", details.getPassword());

        // une fois la graine tournee, getPassword() ne doit plus jamais renvoyer l'id
        u.setAuthTokenSeed("rotated-seed-xyz");
        OAuth2UserDetails rotated = OAuth2UserDetails.create(u);
        assertEquals("rotated-seed-xyz", rotated.getPassword());
        assertNotEquals(u.getId(), rotated.getPassword());

    }

    @Test
    public void testIsEnabledReflectsDeletion() {

        User active = new User();
        active.setId("id1");
        active.setDeletion(false);
        assertTrue(OAuth2UserDetails.create(active).isEnabled());

        User deleted = new User();
        deleted.setId("id2");
        deleted.setDeletion(true);
        assertFalse(OAuth2UserDetails.create(deleted).isEnabled());

    }

    @Test
    public void testAttributesExposeAccountCompletionState() {

        User u = new User();
        u.setId("id1");
        u.setVerifiedEmail("user@example.com");
        u.setPasswordHash("hash");

        OAuth2UserDetails details = OAuth2UserDetails.create(u);

        assertEquals("user@example.com", details.getAttributes().get("email"));
        assertEquals(true, details.getAttributes().get("emailVerified"));
        assertEquals(true, details.getAttributes().get("passwordDefined"));
        assertEquals(true, details.getAttributes().get("accountComplete"));

    }

    @Test
    public void testAttributesExposeIncompleteAccount() {

        User u = new User();
        u.setId("id1");

        OAuth2UserDetails details = OAuth2UserDetails.create(u);

        assertEquals(false, details.getAttributes().get("emailVerified"));
        assertEquals(false, details.getAttributes().get("passwordDefined"));
        assertEquals(false, details.getAttributes().get("accountComplete"));

    }

    /**
     * Les sessions sont serialisees en base (spring.session.store-type=jdbc) : toute evolution de
     * la classe qui changerait l'UID calcule invaliderait silencieusement toutes les sessions
     * vivantes. Valeur figee par serialver sur la version d'origine de la classe.
     */
    @Test
    public void testSerialVersionUidIsFrozen() throws NoSuchFieldException, IllegalAccessException {

        Field field = OAuth2UserDetails.class.getDeclaredField("serialVersionUID");
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertTrue(Modifier.isFinal(field.getModifiers()));
        field.setAccessible(true);
        assertEquals(6064613477799000059L, field.getLong(null));

    }
}
