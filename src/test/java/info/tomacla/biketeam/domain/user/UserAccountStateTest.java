package info.tomacla.biketeam.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaire pur (aucun contexte Spring, aucun Testcontainers) des regles metier de
 * completion de compte portees par {@link User}. A ne pas confondre avec UserTest, qui est
 * un test Testcontainers/@DataJpaTest (voir AbstractDBTest).
 */
public class UserAccountStateTest {

    private User newUser() {
        User u = new User();
        u.setId("id1");
        return u;
    }

    @Test
    public void testIncompleteByDefault() {
        User u = newUser();
        assertFalse(u.hasPasswordLogin());
        assertFalse(u.hasExternalIdentity());
        assertFalse(u.isAccountComplete());
    }

    @Test
    public void testEmailAloneNotEnough() {
        User u = newUser();
        u.setEmail("user@example.com");
        // pas verifie, pas de mot de passe
        assertFalse(u.hasPasswordLogin());
        assertFalse(u.isAccountComplete());
    }

    @Test
    public void testEmailVerifiedWithoutPasswordNotEnough() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        assertTrue(u.isEmailVerified());
        assertFalse(u.hasPasswordLogin());
        assertFalse(u.isAccountComplete());
    }

    @Test
    public void testPasswordWithoutVerifiedEmailNotEnough() {
        User u = newUser();
        u.setEmail("user@example.com");
        u.setPasswordHash("hash");
        assertFalse(u.isEmailVerified());
        assertFalse(u.hasPasswordLogin());
        assertFalse(u.isAccountComplete());
    }

    @Test
    public void testEmailVerifiedAndPasswordComplete() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        u.setPasswordHash("hash");
        assertTrue(u.hasPasswordLogin());
        assertFalse(u.hasExternalIdentity());
        assertTrue(u.isAccountComplete());
    }

    @Test
    public void testGoogleIdMakesComplete() {
        User u = newUser();
        u.setGoogleId("google1");
        assertFalse(u.hasPasswordLogin());
        assertTrue(u.hasExternalIdentity());
        assertTrue(u.isAccountComplete());
    }

    @Test
    public void testFacebookIdMakesComplete() {
        User u = newUser();
        u.setFacebookId("fb1");
        assertTrue(u.hasExternalIdentity());
        assertTrue(u.isAccountComplete());
    }

    @Test
    public void testSetEmailNormalizesTrimAndLowercase() {
        User u = newUser();
        u.setEmail("  User@Example.COM  ");
        assertEquals("user@example.com", u.getEmail());
    }

    @Test
    public void testSetEmailInvalidBecomesNull() {
        User u = newUser();
        u.setEmail("not-an-email");
        assertNull(u.getEmail());
    }

    @Test
    public void testSetEmailResetsVerifiedFlagWhenValueChanges() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        assertTrue(u.isEmailVerified());

        u.setEmail("other@example.com");
        assertFalse(u.isEmailVerified());
    }

    @Test
    public void testSetEmailSameValueDoesNotResetVerifiedFlag() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        assertTrue(u.isEmailVerified());

        // meme valeur (a la casse et aux espaces pres) : ne doit pas retomber a false
        u.setEmail("  User@Example.com ");
        assertTrue(u.isEmailVerified());
    }

    @Test
    public void testSetEmailNullResetsVerifiedFlag() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        u.setEmail(null);
        assertNull(u.getEmail());
        assertFalse(u.isEmailVerified());
    }

    @Test
    public void testSetVerifiedEmailSetsVerifiedTrue() {
        User u = newUser();
        u.setVerifiedEmail("user@example.com");
        assertEquals("user@example.com", u.getEmail());
        assertTrue(u.isEmailVerified());
    }

    @Test
    public void testSetVerifiedEmailWithInvalidAddressDoesNotVerify() {
        User u = newUser();
        u.setVerifiedEmail("not-an-email");
        assertNull(u.getEmail());
        assertFalse(u.isEmailVerified());
    }

}
