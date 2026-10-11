package info.tomacla.biketeam.service.pedalons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Appels M2M de biketeam vers Pedalons, en HTTPS par son entree publique : declenchement et suivi d'un job.
 * <p>
 * Authentification par l'en-tete X-Biketeam-Migration-Secret (jamais Authorization : Pedalons
 * le lirait comme un JWT). Les reponses 4xx/5xx ne levent pas d'exception : l'appelant decide
 * selon le statut et le champ "code" du corps. Une erreur reseau donne un statut 0.
 */
@Component
public class PedalonsClient {

    private static final Logger log = LoggerFactory.getLogger(PedalonsClient.class);

    public static final String SECRET_HEADER = "X-Biketeam-Migration-Secret";
    public static final String JOBS_PATH = "/api/internal/biketeam-migration/jobs";
    /** Delais reseau de chaque appel ; bornent la duree d'un declenchement (PedalonsMigrationService). */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final PedalonsProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Autowired
    public PedalonsClient(PedalonsProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, buildRestTemplate());
    }

    PedalonsClient(PedalonsProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
                // jamais appele : hasError repond toujours false
            }
        });
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return new RestTemplate(factory);
    }

    /**
     * Reponse de Pedalons. status vaut 0 si Pedalons n'a pas pu etre joint (reseau, delai).
     */
    public record Response(int status, JsonNode body, String rawBody) {

        public boolean isNetworkError() {
            return status == 0;
        }

        public boolean isServerError() {
            return status >= 500;
        }

        public boolean isRetryable() {
            return isNetworkError() || isServerError();
        }

        /**
         * Champ "code" du corps d'erreur (ErrorResponse de Pedalons), ou null.
         */
        public String code() {
            return text("code");
        }

        public String text(String field) {
            if (body == null || !body.hasNonNull(field)) {
                return null;
            }
            return body.get(field).asText();
        }
    }

    /**
     * POST /api/internal/biketeam-migration/jobs : 202 (job cree) ou 200 (rejeu idempotent).
     */
    public Response trigger(String requestId, String grant, String teamId, boolean dryRun, boolean reset) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requestId", requestId);
        body.put("grant", grant);
        body.put("teamId", teamId);
        body.put("dryRun", dryRun);
        body.put("reset", reset);
        return exchange(HttpMethod.POST, properties.getInternalUrl() + JOBS_PATH, body);
    }

    /**
     * GET /api/internal/biketeam-migration/jobs/{jobId}.
     */
    public Response status(String jobId) {
        return exchange(HttpMethod.GET, properties.getInternalUrl() + JOBS_PATH + "/"
                + UriUtils.encodePathSegment(jobId, StandardCharsets.UTF_8), null);
    }

    private Response exchange(HttpMethod method, String url, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(SECRET_HEADER, properties.getTriggerSecret());
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        try {
            final ResponseEntity<String> response = restTemplate.exchange(java.net.URI.create(url), method,
                    new HttpEntity<>(body == null ? null : objectMapper.writeValueAsString(body), headers), String.class);
            final String raw = response.getBody();
            return new Response(response.getStatusCode().value(), parse(raw), raw);
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException e) {
            // ne jamais journaliser les en-tetes : ils portent le secret
            log.warn("Pedalons {} {} failed: {}", method, url, e.getMessage());
            return new Response(0, NullNode.getInstance(), null);
        }
    }

    private JsonNode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return NullNode.getInstance();
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            return NullNode.getInstance();
        }
    }

}
