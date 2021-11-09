package info.tomacla.biketeam.security.session;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SSOToken {

    private String token;
    private ZonedDateTime generatedAt;
    private String sessionId;
    private String rememberMe;

    private SSOToken(String token, ZonedDateTime generatedAt, String sessionId, String rememberMe) {
        this.token = token;
        this.generatedAt = generatedAt;
        this.sessionId = sessionId;
        this.rememberMe = rememberMe;
    }

    public static SSOToken create(String sessionId, String rememberMe) {
        return new SSOToken(UUID.randomUUID().toString(),
                ZonedDateTime.now(ZoneOffset.UTC),
                sessionId,
                rememberMe);
    }

    public boolean isValid() {
        return ChronoUnit.SECONDS.between(generatedAt, ZonedDateTime.now(ZoneOffset.UTC)) <= 20L;
    }

    public String getToken() {
        return token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRememberMe() {
        return rememberMe;
    }
}