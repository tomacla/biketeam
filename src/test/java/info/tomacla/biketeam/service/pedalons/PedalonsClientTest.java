package info.tomacla.biketeam.service.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Appels M2M vers Pedalons (section 5 du plan) : en-tete, corps, et aucune exception sur un refus.
 */
public class PedalonsClientTest {

    private static final String KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    private MockRestServiceServer server;
    private PedalonsClient client;

    @BeforeEach
    public void setUp() {
        PedalonsProperties properties = new PedalonsProperties("https://www.pedalons.fr", "https://www.pedalons.fr/", KEY,
                "trigger-secret", "export-secret", "https://biketeam.example");
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new PedalonsClient(properties, new ObjectMapper(), restTemplate);
    }

    @Test
    public void testTriggerSendsTheContractBodyAndSecret() {
        server.expect(requestTo("https://www.pedalons.fr/api/internal/biketeam-migration/jobs"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(PedalonsClient.SECRET_HEADER, "trigger-secret"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andExpect(jsonPath("$.requestId").value("req-1"))
                .andExpect(jsonPath("$.grant").value("bmg_x"))
                .andExpect(jsonPath("$.teamId").value("n-peloton"))
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.reset").value(false))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jobId\":\"0hx3k2m9q8r4t\",\"status\":\"QUEUED\",\"targetTeamSlug\":\"n-peloton\"}"));

        PedalonsClient.Response response = client.trigger("req-1", "bmg_x", "n-peloton", true, false);

        assertEquals(202, response.status());
        assertEquals("0hx3k2m9q8r4t", response.text("jobId"));
        server.verify();
    }

    @Test
    public void testRefusalIsReturnedNotThrown() {
        server.expect(requestTo("https://www.pedalons.fr/api/internal/biketeam-migration/jobs"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"BIKETEAM_MIGRATION_RUNNING\",\"activeJobId\":\"abc\"}"));

        PedalonsClient.Response response = client.trigger("req-1", "bmg_x", "n-peloton", false, false);

        assertEquals(409, response.status());
        assertEquals("BIKETEAM_MIGRATION_RUNNING", response.code());
        assertFalse(response.isRetryable());
    }

    @Test
    public void testStatusAndServerErrors() {
        server.expect(requestTo("https://www.pedalons.fr/api/internal/biketeam-migration/jobs/0hx3k2m9q8r4t"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(PedalonsClient.SECRET_HEADER, "trigger-secret"))
                .andRespond(withServerError());

        PedalonsClient.Response response = client.status("0hx3k2m9q8r4t");

        assertEquals(500, response.status());
        assertTrue(response.isRetryable());
        assertNull(response.code());
    }

    @Test
    public void testNetworkErrorGivesStatusZero() {
        server.expect(requestTo("https://www.pedalons.fr/api/internal/biketeam-migration/jobs/job"))
                .andRespond(request -> {
                    throw new IOException("connection refused");
                });

        PedalonsClient.Response response = client.status("job");

        assertEquals(0, response.status());
        assertTrue(response.isNetworkError());
        assertTrue(response.isRetryable());
    }

}
