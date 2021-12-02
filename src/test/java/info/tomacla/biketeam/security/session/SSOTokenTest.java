package info.tomacla.biketeam.security.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SSOTokenTest {

    @Test
    public void test() throws InterruptedException {
        SSOToken token = SSOToken.create("sessionId", "rememberMe");
        assertTrue(token.isValid(1L));

        assertEquals("sessionId", token.getSessionId());
        assertEquals("rememberMe", token.getRememberMe());

        Thread.sleep(1000L);
        assertFalse(token.isValid(1L));
    }

}
