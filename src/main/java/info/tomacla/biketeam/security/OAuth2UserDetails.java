package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.*;

public class OAuth2UserDetails implements Serializable, UserDetails, OAuth2User {

    private List<GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Override
    public String getName() {
        return getAttribute("identity");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return getAttribute("id");
    }

    @Override
    public String getUsername() {
        return getAttribute("id");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static OAuth2UserDetails create(User u) {

        OAuth2UserDetails ud = new OAuth2UserDetails();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("id", u.getId());
        attrs.put("admin", u.isAdmin());
        attrs.put("city", u.getCity());
        attrs.put("lastName", u.getLastName());
        attrs.put("firstName", u.getFirstName());
        attrs.put("identity", u.getIdentity());
        attrs.put("stravaId", u.getStravaId());
        attrs.put("facebookId", u.getFacebookId());
        attrs.put("googleId", u.getGoogleId());
        attrs.put("stravaUserName", u.getStravaUserName());
        ud.setAttributes(attrs);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(Authorities.user());
        if (u.isAdmin()) {
            authorities.add(Authorities.admin());
        }

        u.getRoles().forEach(role -> {
            authorities.add(Authorities.teamUser(role.getTeam().getId()));
            if (role.getRole().equals(Role.ADMIN)) {
                authorities.add(Authorities.teamAdmin(role.getTeam().getId()));
            }
        });

        ud.setAuthorities(authorities);

        return ud;

    }

}
