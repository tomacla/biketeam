package info.tomacla.biketeam.security;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SSOToken {

    private String token;
    private ZonedDateTime generatedAt;
    private String originToken;

    private SSOToken(String token, ZonedDateTime generatedAt, String originToken) {
        this.token = token;
        this.generatedAt = generatedAt;
        this.originToken = originToken;
    }

    public static SSOToken create(String originToken) {
        return new SSOToken(UUID.randomUUID().toString(),
                ZonedDateTime.now(ZoneOffset.UTC),
                originToken == null ? "none" : originToken);
    }

    public boolean isValid() {
        return ChronoUnit.SECONDS.between(generatedAt, ZonedDateTime.now(ZoneOffset.UTC)) <= 20L;
    }

    public String getToken() {
        return token;
    }

    public String getOriginToken() {
        return originToken;
    }

}