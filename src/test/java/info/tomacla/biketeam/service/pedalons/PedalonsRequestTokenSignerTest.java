package info.tomacla.biketeam.service.pedalons;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jeton de demande : vecteur de test du plan de migration (repris a l'identique cote Pedalons,
 * qui doit l'accepter). Un octet de difference et la signature ne verifie plus.
 */
public class PedalonsRequestTokenSignerTest {

    static final String KEY_BASE64 = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    static final String PAYLOAD = "{\"iss\":\"biketeam\",\"aud\":\"pedalons:biketeam-migration\",\"ver\":1,"
            + "\"jti\":\"6f1c2a3e-0000-4000-8000-000000000001\",\"iat\":1790000000,\"exp\":1790000900,"
            + "\"teamId\":\"n-peloton\",\"teamName\":\"N'Peloton\",\"requestedBy\":\"Jane D.\",\"dryRun\":true,"
            + "\"reset\":false,\"returnUrl\":\"https://biketeam.example/n-peloton/admin/pedalons/callback\","
            + "\"summary\":{\"places\":1,\"routes\":2,\"rides\":3,\"rideTemplates\":0,\"trips\":1,\"tripStages\":2,"
            + "\"publications\":4,\"faqPage\":true,\"logo\":false}}";

    static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
            + "eyJpc3MiOiJiaWtldGVhbSIsImF1ZCI6InBlZGFsb25zOmJpa2V0ZWFtLW1pZ3JhdGlvbiIsInZlciI6MSwianRpIjoiNmYxYzJhM2UtMDAwMC00MDAwLTgwMDAtMDAwMDAwMDAwMDAxIiwiaWF0IjoxNzkwMDAwMDAwLCJleHAiOjE3OTAwMDA5MDAsInRlYW1JZCI6Im4tcGVsb3RvbiIsInRlYW1OYW1lIjoiTidQZWxvdG9uIiwicmVxdWVzdGVkQnkiOiJKYW5lIEQuIiwiZHJ5UnVuIjp0cnVlLCJyZXNldCI6ZmFsc2UsInJldHVyblVybCI6Imh0dHBzOi8vYmlrZXRlYW0uZXhhbXBsZS9uLXBlbG90b24vYWRtaW4vcGVkYWxvbnMvY2FsbGJhY2siLCJzdW1tYXJ5Ijp7InBsYWNlcyI6MSwicm91dGVzIjoyLCJyaWRlcyI6MywicmlkZVRlbXBsYXRlcyI6MCwidHJpcHMiOjEsInRyaXBTdGFnZXMiOjIsInB1YmxpY2F0aW9ucyI6NCwiZmFxUGFnZSI6dHJ1ZSwibG9nbyI6ZmFsc2V9fQ."
            + "-Fy6Ljdr2ChwKFIAAkH3lh8CIUynNQFNJm2Us2G-jlw";

    private static PedalonsProperties properties(String siteUrl) {
        return new PedalonsProperties("https://www.pedalons.fr", "https://www.pedalons.fr", KEY_BASE64,
                "trigger-secret", "export-secret", siteUrl);
    }

    @Test
    public void testSignatureMatchesTheContractVector() {
        assertEquals(TOKEN, PedalonsRequestTokenSigner.sign(PAYLOAD, Base64.getDecoder().decode(KEY_BASE64)));
    }

    @Test
    public void testPayloadIsSerializedByteForByte() {
        PedalonsRequestTokenSigner signer = new PedalonsRequestTokenSigner(properties("https://biketeam.example"));

        // exp = iat + 3600 dans le jeton reel ; le vecteur a exp = iat + 900 : on ne compare que la charge
        // sans exp, puis la signature complete sur le vecteur
        String payload = signer.payload("6f1c2a3e-0000-4000-8000-000000000001", 1790000000L, "n-peloton", "N'Peloton",
                "Jane D.", true, false, "https://biketeam.example/n-peloton/admin/pedalons/callback",
                new PedalonsSummary(1, 2, 3, 0, 1, 2, 4, true, false));

        assertEquals(PAYLOAD.replace("\"exp\":1790000900", "\"exp\":1790003600"), payload);
    }

    @Test
    public void testCreatedTokenCarriesTheSignedReturnUrl() {
        PedalonsRequestTokenSigner signer = new PedalonsRequestTokenSigner(properties("https://biketeam.example/"));

        String token = signer.createToken("6f1c2a3e-0000-4000-8000-000000000001", Instant.ofEpochSecond(1790000000L),
                "n-peloton", "N'Peloton", "Jane D.", true, false, new PedalonsSummary(1, 2, 3, 0, 1, 2, 4, true, false));

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        assertEquals("{\"alg\":\"HS256\",\"typ\":\"JWT\"}",
                new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"returnUrl\":\"https://biketeam.example/n-peloton/admin/pedalons/callback\""));
        assertTrue(payload.contains("\"exp\":1790003600"));
        assertFalse(token.contains("="), "base64url without padding");
        // la signature porte sur les segments recus
        assertEquals(token, PedalonsRequestTokenSigner.sign(payload, Base64.getDecoder().decode(KEY_BASE64)));
    }

}
