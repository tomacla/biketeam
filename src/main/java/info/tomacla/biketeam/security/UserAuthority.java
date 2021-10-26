package info.tomacla.biketeam.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

public abstract class UserAuthority {

    public static GrantedAuthority get() {
        return new SimpleGrantedAuthority("ROLE_USER");
    }

    public static boolean check(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(get());
    }

}
