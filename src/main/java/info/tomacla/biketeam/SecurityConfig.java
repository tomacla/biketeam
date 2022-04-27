package info.tomacla.biketeam;

import info.tomacla.biketeam.security.login.CustomLoginUrlAuthenticationEntryPoint;
import info.tomacla.biketeam.security.oauth2.OAuth2StateWriter;
import info.tomacla.biketeam.security.oauth2.OAuth2SuccessHandler;
import info.tomacla.biketeam.security.session.CookieHttpSessionIdResolverWithSSO;
import info.tomacla.biketeam.security.session.SSOService;
import info.tomacla.biketeam.security.session.SSOTokenFilter;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Field;

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
    private SSOService ssoService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // global conf
        http.cors()
                .and()
                .csrf().disable();

        // requests conf
        http.authorizeRequests(auth -> {

            auth.antMatchers("/css/**").permitAll();
            auth.antMatchers("/js/**").permitAll();
            auth.antMatchers("/teams").permitAll();
            auth.antMatchers("/login/**").permitAll();
            auth.antMatchers("/logout").permitAll();
            auth.antMatchers("/legal-mentions").permitAll();
            auth.antMatchers("/*/image").permitAll();

            auth.antMatchers("/api/**").permitAll();

            auth.antMatchers("/users/me").authenticated();
            auth.antMatchers("/users/me/delete").authenticated();
            auth.antMatchers("/new").authenticated();

            auth.antMatchers("/admin/**").hasRole("ADMIN");
            auth.antMatchers("/management/**").hasRole("ADMIN");
            auth.antMatchers("/{teamId}/admin/**").access("@userService.authorizeAdminAccess(authentication, #teamId)");
            auth.antMatchers("/{teamId}/**").access("@userService.authorizePublicAccess(authentication, #teamId)");
            auth.anyRequest().permitAll();
        });

        // handle unauthorized
        http.exceptionHandling(e -> {
            // user is not authenticated
            e.authenticationEntryPoint(new CustomLoginUrlAuthenticationEntryPoint("/login"));
            // user has not access to resource
            e.accessDeniedPage("/?error=Acc%C3%A8s%20interdit");
        });

        // remember me conf
        http.rememberMe(rm -> {
            rm.alwaysRemember(true);
            rm.userDetailsService(userDetailsService);
        });

        // oauth2 conf
        http.oauth2Login(oauth2 -> {
            oauth2.failureUrl("/?error=Erreur%20de%20connexion");
            oauth2.loginPage("/login");
            oauth2.authorizationEndpoint(config -> config.authorizationRequestResolver(oAuth2AuthorizationRequestResolver()));
            oauth2.successHandler(oAuth2SuccessHandler());
        });

        // logout
        http.logout(logout -> logout.logoutSuccessUrl("/"));


    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(ssoService);
    }

    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcOperations jdbcOperations, ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
    }


    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver() {
        Field field = ReflectionUtils.findField(DefaultOAuth2AuthorizationRequestResolver.class, "stateGenerator");
        ReflectionUtils.makeAccessible(field);

        DefaultOAuth2AuthorizationRequestResolver defaultOAuth2AuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        ReflectionUtils.setField(field, defaultOAuth2AuthorizationRequestResolver, oAuth2StateWriter());
        return defaultOAuth2AuthorizationRequestResolver;
    }

    @Bean
    public CookieHttpSessionIdResolverWithSSO customCookieHttpSessionIdResolver() {
        return new CookieHttpSessionIdResolverWithSSO(urlService, ssoService);
    }

    @Bean
    public SSOTokenFilter ssoTokenFilter() {
        return new SSOTokenFilter();
    }

    @Bean
    public OAuth2StateWriter oAuth2StateWriter() {
        return new OAuth2StateWriter(urlService, teamService);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("*");
            }
        };
    }

}