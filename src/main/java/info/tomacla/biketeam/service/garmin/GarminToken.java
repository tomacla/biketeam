package info.tomacla.biketeam.service.garmin;

import com.github.scribejava.core.model.OAuth1AccessToken;

import java.io.Serializable;

public record GarminToken(String userId, OAuth1AccessToken accessToken) implements Serializable {
}
