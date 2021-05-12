package info.tomacla.biketeam.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class LocalDefaultOAuth2User extends DefaultOAuth2User {

    private final String localUserId;

    public LocalDefaultOAuth2User(Collection<? extends GrantedAuthority> authorities,
                                  Map<String, Object> attributes,
                                  String nameAttributeKey,
                                  String localUserId) {
        super(authorities, attributes, nameAttributeKey);
        this.localUserId = localUserId;
    }

    public String getLocalUserId() {
        return localUserId;
    }
}
