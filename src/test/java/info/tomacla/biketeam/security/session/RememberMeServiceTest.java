package info.tomacla.biketeam.security.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RememberMeServiceTest {

    private static final String REMEMBER_ME_KEY = "test-key";
    private static final String USERNAME = "user-1";
    private static final String PASSWORD_SEED = "seed-value";

    private RememberMeService service;
    private UserDetailsService userDetailsService;

    @BeforeEach
    public void setUp() throws Exception {

        service = new RememberMeService();

        Field keyField = RememberMeService.class.getDeclaredField("rememberMeKey");
        keyField.setAccessible(true);
        keyField.set(service, REMEMBER_ME_KEY);

        userDetailsService = mock(UserDetailsService.class);
        Field udsField = RememberMeService.class.getDeclaredField("userDetailsService");
        udsField.setAccessible(true);
        udsField.set(service, userDetailsService);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(USERNAME, PASSWORD_SEED, true, true, true, true, authorities);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

    }

    private String buildCookie(long expiry, String algorithmJca, String algorithmToken, boolean fourTokens) throws NoSuchAlgorithmException {

        String data = USERNAME + ":" + expiry + ":" + PASSWORD_SEED + ":" + REMEMBER_ME_KEY;
        MessageDigest digest = MessageDigest.getInstance(algorithmJca);
        String signature = new String(Hex.encode(digest.digest(data.getBytes(StandardCharsets.UTF_8))));

        String plainText = fourTokens
                ? USERNAME + ":" + expiry + ":" + algorithmToken + ":" + signature
                : USERNAME + ":" + expiry + ":" + signature;

        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));

    }

    @Test
    public void testFourTokenSha256CookieAccepted() throws Exception {

        long expiry = System.currentTimeMillis() + 100_000L;
        String cookie = buildCookie(expiry, "SHA-256", "SHA256", true);

        Authentication authentication = service.getUserDetailsFromRememberMe(cookie);

        assertNotNull(authentication);
        assertEquals(USERNAME, authentication.getName());

    }

    @Test
    public void testThreeTokenMd5CookieAcceptedForBackwardCompatibility() throws Exception {

        long expiry = System.currentTimeMillis() + 100_000L;
        String cookie = buildCookie(expiry, "MD5", null, false);

        Authentication authentication = service.getUserDetailsFromRememberMe(cookie);

        assertNotNull(authentication);
        assertEquals(USERNAME, authentication.getName());

    }

    @Test
    public void testWrongSignatureRejected() throws Exception {

        long expiry = System.currentTimeMillis() + 100_000L;
        String validCookie = buildCookie(expiry, "SHA-256", "SHA256", true);

        // corrompt la signature en fin de payload
        String corrupted = validCookie.substring(0, validCookie.length() - 4) + "0000";

        assertThrows(RuntimeException.class, () -> service.getUserDetailsFromRememberMe(corrupted));

    }

    @Test
    public void testExpiredCookieRejected() throws Exception {

        long expiry = System.currentTimeMillis() - 100_000L;
        String cookie = buildCookie(expiry, "SHA-256", "SHA256", true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getUserDetailsFromRememberMe(cookie));
        assertTrue(ex.getMessage().contains("expired"));

    }

}
