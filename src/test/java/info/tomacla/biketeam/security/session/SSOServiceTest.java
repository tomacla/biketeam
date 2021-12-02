package info.tomacla.biketeam.security.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SSOServiceTest {

    @Test
    public void test() {

        SSOService ssoService = new SSOService();

        assertTrue(ssoService.getSessionIdFromSSOToken("sso").isEmpty());
        assertTrue(ssoService.getRememberMeFromSSOToken("sso").isEmpty());

        String ssoToken = ssoService.getSSOToken("sso", null);
        assertFalse(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertTrue(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

        ssoToken = ssoService.getSSOToken("sso", "rm");
        assertFalse(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertFalse(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

        assertTrue(ssoService.getSessionIdFromSSOToken("sso").isEmpty());
        assertTrue(ssoService.getRememberMeFromSSOToken("sso").isEmpty());


    }

    @Test
    public void testExpiracy() throws InterruptedException {
        SSOService ssoService = new SSOService(1L);
        String ssoToken = ssoService.getSSOToken("sso", "rm");

        assertFalse(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertFalse(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

        Thread.sleep(1100L);

        assertTrue(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertTrue(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

    }

    @Test
    public void testNotExpiracy() throws InterruptedException {
        SSOService ssoService = new SSOService(1L);
        String ssoToken = ssoService.getSSOToken("sso", "rm");

        assertFalse(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertFalse(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

        Thread.sleep(750L);

        assertFalse(ssoService.getSessionIdFromSSOToken(ssoToken).isEmpty());
        assertFalse(ssoService.getRememberMeFromSSOToken(ssoToken).isEmpty());

    }

}
