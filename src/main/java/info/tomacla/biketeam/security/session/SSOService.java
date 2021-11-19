package info.tomacla.biketeam.security.session;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SSOService {

    // IMPROVE use a guava cache instead of this
    // TODO should be stored in case of load balancing
    private Map<String, SSOToken> ssoTokens = new HashMap<>();

    public String getSSOToken(String sessionId, String rememberMe) {
        SSOToken ssoToken = SSOToken.create(sessionId, rememberMe);
        ssoTokens.put(ssoToken.getToken(), ssoToken);
        return ssoToken.getToken();
    }

    public Optional<String> getSessionIdFromSSOToken(String ssoTokenString) {
        purgeTokens();
        if (ssoTokenString != null) {
            SSOToken ssoToken = ssoTokens.get(ssoTokenString);
            if (ssoToken != null && ssoToken.isValid()) {
                return Optional.ofNullable(ssoToken.getSessionId());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getRememberMeFromSSOToken(String ssoTokenString) {
        purgeTokens();
        if (ssoTokenString != null) {
            SSOToken ssoToken = ssoTokens.get(ssoTokenString);
            if (ssoToken != null && ssoToken.isValid()) {
                return Optional.ofNullable(ssoToken.getRememberMe());
            }
        }
        return Optional.empty();
    }

    private void purgeTokens() {
        ssoTokens.entrySet().removeIf(e -> !e.getValue().isValid());
    }

}
