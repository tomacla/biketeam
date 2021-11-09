package info.tomacla.biketeam.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public abstract class Authorities {

    public static GrantedAuthority admin() {
        return new SimpleGrantedAuthority("ROLE_ADMIN");
    }

    public static GrantedAuthority user() {
        return new SimpleGrantedAuthority("ROLE_USER");
    }

    public static GrantedAuthority teamAdmin(String teamId) {
        return new SimpleGrantedAuthority("ROLE_ADMIN_" + teamId);
    }

}
