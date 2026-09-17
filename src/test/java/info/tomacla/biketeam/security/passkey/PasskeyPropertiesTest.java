package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.data.client.Origin;
import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PasskeyPropertiesTest {

    private PasskeyProperties properties(String siteUrl, String cookieDomain, String rpId, String origins) {
        UrlService urlService = mock(UrlService.class);
        when(urlService.getSiteUrl()).thenReturn(siteUrl);
        when(urlService.getCookieDomain()).thenReturn(cookieDomain);

        PasskeyProperties properties = new PasskeyProperties();
        ReflectionTestUtils.setField(properties, "urlService", urlService);
        ReflectionTestUtils.setField(properties, "siteName", "BikeTeam");
        ReflectionTestUtils.setField(properties, "configuredRpId", rpId);
        ReflectionTestUtils.setField(properties, "configuredOrigins", origins);
        ReflectionTestUtils.setField(properties, "challengeValiditySeconds", 300);
        ReflectionTestUtils.setField(properties, "maxPerUser", 20);
        ReflectionTestUtils.setField(properties, "loginOptionsMaxPerHour", 60);
        properties.init();
        return properties;
    }

    @Test
    public void testRpIdDefaultsToSiteHost() {
        PasskeyProperties p = properties("https://biketeam.info", "biketeam.info", "", "");
        assertEquals("biketeam.info", p.getRpId());
        assertTrue(p.getOrigins().contains(Origin.create("https://biketeam.info")));
    }

    @Test
    public void testConfiguredRpIdWins() {
        PasskeyProperties p = properties("https://www.biketeam.info", "www.biketeam.info", "biketeam.info", "");
        assertEquals("biketeam.info", p.getRpId());
    }

    /**
     * Un slash final dans site.url ne doit pas se retrouver dans l'origine : la comparaison avec
     * celle annoncee par le navigateur est stricte.
     */
    @Test
    public void testTrailingSlashIsStrippedFromOrigin() {
        PasskeyProperties p = properties("https://biketeam.info/", "biketeam.info", "", "");
        assertTrue(p.getOrigins().contains(Origin.create("https://biketeam.info")));
    }

    @Test
    public void testAdditionalOriginsAreAccepted() {
        PasskeyProperties p = properties("https://biketeam.info", "biketeam.info", "",
                "http://localhost:8080, https://staging.biketeam.info");
        assertEquals(3, p.getOrigins().size());
        assertTrue(p.getOrigins().contains(Origin.create("http://localhost:8080")));
        assertTrue(p.getOrigins().contains(Origin.create("https://staging.biketeam.info")));
    }

}
