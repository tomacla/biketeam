package info.tomacla.biketeam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Le PasswordEncoder vit hors de SecurityConfig, expres.
 * <p>
 * SecurityConfig injecte UserDetailsService -> UserService, et UserService#init() encode le mot de
 * passe du compte admin.email des le demarrage. Declare dans SecurityConfig, l'encodeur exigeait
 * donc SecurityConfig pendant sa propre creation : cycle de beans, et l'application refusait de
 * demarrer tant que ce compte n'existait pas (voir ApplicationContextStartupTest).
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
