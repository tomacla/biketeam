package info.tomacla.biketeam.security;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class SSOService {

    // IMPROVE use a guava cache instead of this
    // FIXME should be stored in case of load balancing
    private Map<String, SSOToken> ssoTokens = new HashMap<>();

    public String getSSOToken(String authToken) {
        SSOToken ssoToken = SSOToken.create(Objects.requireNonNull(authToken));
        ssoTokens.put(ssoToken.getToken(), ssoToken);
        return ssoToken.getToken();
    }

    public Optional<String> getAuthTokenFromSSOToken(String ssoTokenString) {
        purgeTokens();
        if (ssoTokenString != null) {
            SSOToken ssoToken = ssoTokens.get(ssoTokenString);
            if (ssoToken != null && ssoToken.isValid()) {
                return Optional.of(ssoToken.getOriginToken());
            }
        }
        return Optional.empty();
    }

    private void purgeTokens() {
        ssoTokens.entrySet().removeIf(e -> !e.getValue().isValid());
    }

}
