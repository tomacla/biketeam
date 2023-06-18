package info.tomacla.biketeam;

import info.tomacla.biketeam.security.login.CustomAccessDeniedHandler;
import info.tomacla.biketeam.security.login.CustomLoginUrlAuthenticationEntryPoint;
import info.tomacla.biketeam.security.session.CustomSessionIdResolver;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${rememberme.key}")
    private String rememberMeKey;

    @Value("${rememberme.validity}")
    private int rememberMeValidity;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // global conf
        http.cors()
                .and()
                .csrf().disable();

        // requests conf
        http.authorizeRequests(auth -> {

            // static
            auth.antMatchers("/css/**").permitAll();
            auth.antMatchers("/js/**").permitAll();
            auth.antMatchers("/img/**").permitAll();
            auth.antMatchers("/*/image").permitAll();
            auth.antMatchers("/legal-mentions").permitAll();

            // GPX tools
            auth.antMatchers("/gpxtool/**").permitAll();

            // spring security endpoints
            auth.antMatchers("/login/**").permitAll();
            auth.antMatchers("/logout").permitAll();

            // Garmin auth
            auth.antMatchers("/auth/**").permitAll();

            // api public endpoints
            auth.antMatchers("/api/data/**").permitAll();
            auth.antMatchers("/api/auth/**").permitAll();
            auth.antMatchers("/api/teams").permitAll();

            // api protected endpoints
            auth.antMatchers("/api/teams/{teamId}/**").access("@userService.authorizePublicAccess(authentication, #teamId)");

            // web public endpoints
            auth.antMatchers("/teams").permitAll();
            auth.antMatchers("/notifications/**").permitAll();
            auth.antMatchers("/autocomplete/**").permitAll();
            auth.antMatchers("/users/*/image").permitAll();

            // web protected endpoints
            auth.antMatchers("/users/me/**").authenticated();
            auth.antMatchers("/new").authenticated();
            auth.antMatchers("/admin/**").hasRole("ADMIN");
            auth.antMatchers("/management/**").hasRole("ADMIN");
            auth.antMatchers("/{teamId}/admin/**").access("@userService.authorizeAdminAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/**/add-participant/**").access("@userService.authorizeAuthenticatedPublicAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/**/remove-participant/**").access("@userService.authorizeAuthenticatedPublicAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/join").access("@userService.authorizeAuthenticatedPublicAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/leave").access("@userService.authorizeAuthenticatedPublicAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/**").access("@userService.authorizePublicAccess(authentication, #teamId)");

            // other request are permitted
            auth.anyRequest().permitAll();

        });

        // handle unauthorized
        http.exceptionHandling(e -> {
            // user is not authenticated
            e.authenticationEntryPoint(new CustomLoginUrlAuthenticationEntryPoint());
            // user has not access to resource
            e.accessDeniedHandler(accessDeniedHandler());
        });

        // remember me conf
        http.rememberMe(rm -> {
            rm.alwaysRemember(true);
            rm.userDetailsService(userDetailsService);
            rm.key(rememberMeKey);
            rm.tokenValiditySeconds(rememberMeValidity);
        });

        // oauth2 conf
        http.oauth2Login();

        // logout
        http.logout(logout -> logout.logoutSuccessUrl("/"));


    }

    @Bean
    public CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcOperations jdbcOperations, ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
    }


    @Bean
    public CustomSessionIdResolver customCookieHttpSessionIdResolver() {
        return new CustomSessionIdResolver(urlService);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .exposedHeaders("X-Pages");
            }
        };
    }

    @Bean
    public AuthenticationManager getAuthenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}