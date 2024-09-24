package info.tomacla.biketeam;

import info.tomacla.biketeam.security.login.CustomAccessDeniedHandler;
import info.tomacla.biketeam.security.login.CustomLoginUrlAuthenticationEntryPoint;
import info.tomacla.biketeam.security.session.CustomSessionIdResolver;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    ApplicationContext applicationContext;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // expression with access to all beans in context (userService, ...)
        SecurityExpressionHandler<RequestAuthorizationContext> expressionHandler = getExpressionHandler();

        // global conf
        http.cors(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);

        // requests conf
        http.authorizeHttpRequests(auth -> {

            // request matchers is using old style ant_path_matcher (see application.properties)

            // static
            auth.requestMatchers("/css/**", "/js/**", "/jsf/**", "/img/**", "/*/image", "/legal-mentions", "/robots.txt", "/misc/**").permitAll();

            // GPX tools
            auth.requestMatchers("/gpxtool/**").permitAll();

            // spring security endpoints
            auth.requestMatchers("/login/**", "/logout").permitAll();

            // Garmin auth
            auth.requestMatchers("/auth/**").permitAll();

            // api public endpoints
            auth.requestMatchers("/api/data/**", "/api/auth/**", "/api/teams").permitAll();

            // api protected endpoints
            auth.requestMatchers("/api/teams/{teamId}/**").access(
                    getWebExpressionAuthorizationManager(expressionHandler, "@userService.authorizePublicAccess(authentication, #teamId)")
            );

            // web public endpoints
            auth.requestMatchers("/teams", "/notifications/**", "/autocomplete/**", "/users/*/image", "/catalog/**", "/confirm-email").permitAll();

            // web protected endpoints
            auth.requestMatchers("/users/me/**", "/users/space/**", "/new").authenticated();
            auth.requestMatchers("/admin/**", "/management/**").hasRole("ADMIN");
            auth.requestMatchers("/{teamId}/admin/**").access(
                    getWebExpressionAuthorizationManager(expressionHandler, "@userService.authorizeAdminAccess(authentication, #teamId)")
            );
            auth.requestMatchers("/{teamId}/**/add-participant/**", "/{teamId}/**/remove-participant/**").access(
                    getWebExpressionAuthorizationManager(expressionHandler, "@userService.authorizeAuthenticatedPublicAccess(authentication, #teamId)")
            );
            auth.requestMatchers("/{teamId}/join", "/{teamId}/leave", "/{teamId}/**").access(
                    getWebExpressionAuthorizationManager(expressionHandler, "@userService.authorizePublicAccess(authentication, #teamId)")
            );

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
        http.oauth2Login(Customizer.withDefaults());

        // logout
        http.logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }

    private DefaultHttpSecurityExpressionHandler getExpressionHandler() {
        DefaultHttpSecurityExpressionHandler expressionHandler = new DefaultHttpSecurityExpressionHandler();
        expressionHandler.setApplicationContext(applicationContext);
        return expressionHandler;
    }

    private WebExpressionAuthorizationManager getWebExpressionAuthorizationManager(SecurityExpressionHandler<RequestAuthorizationContext> expressionHandler, String expression) {
        WebExpressionAuthorizationManager authorizationManager = new WebExpressionAuthorizationManager(expression);
        authorizationManager.setExpressionHandler(expressionHandler);
        return authorizationManager;
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
