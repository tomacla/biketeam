package info.tomacla.biketeam.security.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasswordPolicyTest {

    @Test
    public void testValidPasswordPasses() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("correcthorse", "correcthorse", "user@example.com"));
    }

    @Test
    public void testTooShort() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate("short1", "short1", null));
        assertEquals(PasswordPolicy.MSG_TOO_SHORT, ex.getMessage());
    }

    @Test
    public void testNullPasswordTooShort() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate(null, null, null));
        assertEquals(PasswordPolicy.MSG_TOO_SHORT, ex.getMessage());
    }

    @Test
    public void testExactly72BytesAscIiPasses() {
        // 72 caracteres ASCII = 72 octets exactement : limite acceptee
        String password = "a".repeat(72);
        assertDoesNotThrow(() -> PasswordPolicy.validate(password, password, null));
    }

    @Test
    public void testTooLongInBytesWithMultiByteCharacters() {
        // chaque "é" occupe 2 octets en UTF-8 : 40 caracteres "é" = 80 octets, mais seulement
        // 40 caracteres de long, donc ce cas ne serait PAS detecte par un controle en longueur
        // de chaine - c'est precisement le controle en octets qui doit s'en charger.
        String password = "é".repeat(40);
        assertEquals(80, password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        assertEquals(40, password.length());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate(password, password, null));
        assertEquals(PasswordPolicy.MSG_TOO_LONG, ex.getMessage());
    }

    @Test
    public void testExactly72MultiByteBoundary() {
        // 24 caracteres "€" (3 octets chacun en UTF-8) = 72 octets exactement : doit passer
        String password = "€".repeat(24);
        assertEquals(72, password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        assertDoesNotThrow(() -> PasswordPolicy.validate(password, password, null));

        // 25 caracteres "€" = 75 octets : doit etre refuse
        String tooLong = "€".repeat(25);
        assertEquals(75, tooLong.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate(tooLong, tooLong, null));
        assertEquals(PasswordPolicy.MSG_TOO_LONG, ex.getMessage());
    }

    @Test
    public void testConfirmationMismatch() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate("correcthorse", "differentvalue", null));
        assertEquals(PasswordPolicy.MSG_MISMATCH, ex.getMessage());
    }

    @Test
    public void testPasswordEqualsEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate("User@Example.com", "User@Example.com", "user@example.com"));
        assertEquals(PasswordPolicy.MSG_CONTAINS_EMAIL, ex.getMessage());
    }

    @Test
    public void testPasswordContainsEmailLocalPart() {
        // "gabriel" (7 caracteres, >= 4) est la local-part de gabriel@example.com
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PasswordPolicy.validate("mygabrielpassword", "mygabrielpassword", "gabriel@example.com"));
        assertEquals(PasswordPolicy.MSG_CONTAINS_EMAIL, ex.getMessage());
    }

    @Test
    public void testShortLocalPartNotRejected() {
        // local-part "abc" (3 caracteres, < 4) : trop court pour etre bloquant, un mot de passe
        // le contenant reste accepte
        assertDoesNotThrow(() -> PasswordPolicy.validate("myabcpasswordxx", "myabcpasswordxx", "abc@example.com"));
    }

    @Test
    public void testNoEmailProvidedSkipsEmailCheck() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("correcthorsebattery", "correcthorsebattery", null));
        assertDoesNotThrow(() -> PasswordPolicy.validate("correcthorsebattery", "correcthorsebattery", ""));
    }

}
