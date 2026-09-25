package info.tomacla.biketeam.service.pedalons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PedalonsPropertiesTest {

    private static final String KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    @Test
    public void testDisabledWhenNothingIsConfigured() {
        PedalonsProperties properties = new PedalonsProperties("", " ", "", "", null, "http://localhost:8080");
        assertFalse(properties.isEnabled());
        assertFalse(properties.isValidExportSecret("anything"));
    }

    @Test
    public void testPartialConfigurationRefusesToStart() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new PedalonsProperties("https://www.pedalons.fr", "", KEY, "", "", "http://localhost:8080"));
        assertTrue(e.getMessage().contains("PEDALONS_INTERNAL_URL"));
        assertTrue(e.getMessage().contains("PEDALONS_TRIGGER_SECRET"));
        assertTrue(e.getMessage().contains("PEDALONS_EXPORT_SECRET"));
        assertFalse(e.getMessage().contains("missing: PEDALONS_URL"));
    }

    @Test
    public void testShortRequestKeyRefusesToStart() {
        assertThrows(IllegalStateException.class,
                () -> new PedalonsProperties("https://p", "https://i", "c2hvcnQ=", "t", "e", "http://localhost:8080"));
        assertThrows(IllegalStateException.class,
                () -> new PedalonsProperties("https://p", "https://i", "not base64 !", "t", "e", "http://localhost:8080"));
    }

    @Test
    public void testPlainHttpInternalUrlOnlyTowardsLocalhost() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new PedalonsProperties("https://p", "http://www.pedalons.fr", KEY, "t", "e", "http://localhost:8080"));
        assertTrue(e.getMessage().contains("PEDALONS_INTERNAL_URL"));
        assertTrue(new PedalonsProperties("https://p", "http://localhost:8080", KEY, "t", "e", "http://localhost:8080").isEnabled());
        assertTrue(new PedalonsProperties("https://p", "http://127.0.0.1:8080", KEY, "t", "e", "http://localhost:8080").isEnabled());
    }

    @Test
    public void testRedirectStatusDefaultsTo302AndAccepts301() {
        assertEquals(302, new PedalonsProperties("", "", "", "", "", "http://localhost:8080").getRedirectStatus());
        assertEquals(302, new PedalonsProperties("", "", "", "", "", "http://localhost:8080", " ").getRedirectStatus());
        assertEquals(301, new PedalonsProperties("", "", "", "", "", "http://localhost:8080", "301").getRedirectStatus());
        assertEquals(302, new PedalonsProperties("https://p", "https://i", KEY, "t", "e", "http://localhost:8080", " 302 ").getRedirectStatus());
    }

    @Test
    public void testOtherRedirectStatusesRefuseToStart() {
        // valide meme fonction eteinte : une faute de frappe se voit au premier demarrage
        for (String invalid : new String[]{"307", "308", "200", "abc", "3O1"}) {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> new PedalonsProperties("", "", "", "", "", "http://localhost:8080", invalid));
            assertTrue(e.getMessage().contains("PEDALONS_REDIRECT_STATUS"));
        }
    }

    @Test
    public void testEnabledConfigurationIsNormalized() {
        PedalonsProperties properties = new PedalonsProperties("https://www.pedalons.fr/", "https://www.pedalons.fr/",
                KEY, "trigger", "export", "https://biketeam.example/");
        assertTrue(properties.isEnabled());
        assertEquals("https://www.pedalons.fr", properties.getUrl());
        assertEquals("https://www.pedalons.fr", properties.getInternalUrl());
        assertEquals("https://biketeam.example", properties.getSiteUrl());
        assertEquals(32, properties.getRequestKey().length);
        assertTrue(properties.isValidExportSecret("export"));
        assertFalse(properties.isValidExportSecret("export "));
        assertFalse(properties.isValidExportSecret("trigger"));
        assertFalse(properties.isValidExportSecret(null));
    }

}
