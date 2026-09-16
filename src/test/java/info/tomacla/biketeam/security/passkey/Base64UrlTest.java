package info.tomacla.biketeam.security.passkey;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Base64UrlTest {

    @Test
    public void testRoundTrip() {
        byte[] value = "biketeam".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(value, Base64Url.decodeOrNull(Base64Url.encode(value)));
    }

    /**
     * Le remplissage doit etre absent : c'est le format impose par WebAuthn, et un "=" final
     * casserait la comparaison avec l'identifiant renvoye par le navigateur.
     */
    @Test
    public void testEncodeHasNoPadding() {
        assertEquals("YQ", Base64Url.encode(new byte[]{'a'}));
    }

    /**
     * L'alphabet base64url ne contient ni + ni / : une valeur ainsi encodee vient forcement
     * d'un client qui ne respecte pas le format.
     */
    @Test
    public void testDecodeRejectsStandardBase64Alphabet() {
        assertNull(Base64Url.decodeOrNull("a+/b"));
    }

    @Test
    public void testDecodeAcceptsPadding() {
        assertArrayEquals(new byte[]{'a'}, Base64Url.decodeOrNull("YQ=="));
    }

    @Test
    public void testDecodeNullAndBlank() {
        assertNull(Base64Url.decodeOrNull(null));
        assertNull(Base64Url.decodeOrNull("   "));
    }

}
