package info.tomacla.biketeam;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationProvider;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationToken;
import info.tomacla.biketeam.security.password.EmailPasswordAuthenticationProvider;
import info.tomacla.biketeam.security.password.LoginAttemptService;
import info.tomacla.biketeam.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Les DEUX providers doivent etre enregistres dans l'AuthenticationManager global.
 * <p>
 * Ce test existe pour une raison precise : Spring Security n'enregistre automatiquement un
 * AuthenticationProvider declare en bean que s'il en trouve <strong>exactement un</strong>
 * (InitializeAuthenticationProviderBeanManagerConfigurer). L'ajout de la connexion par passkey
 * porte ce nombre a deux : sans le bean {@code authenticationProvidersConfigurer} de
 * {@link SecurityConfig}, AUCUN des deux ne serait enregistre et la connexion par email et mot
 * de passe cesserait de fonctionner, sans la moindre erreur au demarrage.
 * <p>
 * Les providers sont enregistres comme singletons deja construits plutot que par des methodes
 * {@code @Bean} : Spring injecterait sinon leurs champs {@code @Autowired}, ce qui exigerait de
 * reconstituer la moitie du contexte applicatif (repositories, services, proprietes).
 */
public class AuthenticationProvidersConfigurationTest {

    private AnnotationConfigApplicationContext context;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() throws Exception {

        final User user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("user@example.com");
        user.setPasswordHash("bcrypt-hash");
        user.setAuthTokenSeed("seed");

        final UserService userService = mock(UserService.class);
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        when(userService.getByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userService.ensureAuthTokenSeed(any(User.class))).thenReturn(user);

        final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("dummy");
        when(passwordEncoder.matches("goodpassword", "bcrypt-hash")).thenReturn(true);

        final EmailPasswordAuthenticationProvider emailProvider = new EmailPasswordAuthenticationProvider();
        ReflectionTestUtils.setField(emailProvider, "userService", userService);
        ReflectionTestUtils.setField(emailProvider, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(emailProvider, "loginAttemptService", mock(LoginAttemptService.class));
        emailProvider.init();

        final PasskeyAuthenticationProvider passkeyProvider = new PasskeyAuthenticationProvider();
        ReflectionTestUtils.setField(passkeyProvider, "userService", userService);

        // tout est enregistre en singletons deja construits, sans aucune classe @Configuration
        // imbriquee : BiketeamApplication declare un @ComponentScan explicite, qui perd le
        // TypeExcludeFilter de Spring Boot. Une configuration de test dans ce paquet serait donc
        // ramassee par le scan de n'importe quel @SpringBootTest a venir.
        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("emailPasswordAuthenticationProvider", emailProvider);
        context.getBeanFactory().registerSingleton("passkeyAuthenticationProvider", passkeyProvider);
        context.getBeanFactory().registerSingleton("authenticationProvidersConfigurer",
                new SecurityConfig().authenticationProvidersConfigurer(emailProvider, passkeyProvider));
        context.register(ObjectPostProcessorConfiguration.class, AuthenticationConfiguration.class);
        context.refresh();

        authenticationManager = context.getBean(AuthenticationConfiguration.class).getAuthenticationManager();

    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testPasswordAuthenticationStillWorksAlongsidePasskey() {

        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("user@example.com", "goodpassword"));

        assertTrue(result.isAuthenticated());
        assertEquals("user-1", ((OAuth2UserDetails) result.getPrincipal()).getUsername());

    }

    @Test
    public void testPasskeyAuthenticationIsRegistered() {

        Authentication result = authenticationManager.authenticate(
                PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1"));

        assertInstanceOf(PasskeyAuthenticationToken.class, result);
        assertTrue(result.isAuthenticated());
        assertEquals("user-1", ((OAuth2UserDetails) result.getPrincipal()).getUsername());

    }

}
