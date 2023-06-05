package info.tomacla.biketeam.service.garmin;

import com.github.scribejava.core.builder.api.DefaultApi10a;

/**
 * Défini comment se connecter en OAuth1 sur Garmin
 */
public class GarminOAuthApi extends DefaultApi10a {

    @Override
    public String getAccessTokenEndpoint() {
        return "https://connectapi.garmin.com/oauth-service/oauth/access_token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://connect.garmin.com/oauthConfirm";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://connectapi.garmin.com/oauth-service/oauth/request_token";
    }

}