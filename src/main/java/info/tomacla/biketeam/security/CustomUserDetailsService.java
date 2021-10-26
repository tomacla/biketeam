package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return OAuth2UserDetails.create(userService.get(s)
                .orElseThrow(() -> new UsernameNotFoundException("Logged user can not be found")));
    }

}
