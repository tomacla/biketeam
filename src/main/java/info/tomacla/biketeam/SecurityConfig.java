package info.tomacla.biketeam;

import info.tomacla.biketeam.security.CustomCookieHttpSessionIdResolver;
import info.tomacla.biketeam.security.OAuth2StateWriter;
import info.tomacla.biketeam.security.OAuth2SuccessHandler;
import info.tomacla.biketeam.security.SSOTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
                .oauth2Login(oauth2 -> {
                    oauth2.failureUrl("/login-error");
                    oauth2.loginPage("/");
                    oauth2.authorizationEndpoint(config ->
                            config.authorizationRequestResolver(oAuth2AuthorizationRequestResolver())
                    );
                    oauth2.successHandler(oAuth2SuccessHandler());
                });

    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler();
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
    public CustomCookieHttpSessionIdResolver customCookieHttpSessionIdResolver() {
        return new CustomCookieHttpSessionIdResolver();
    }

    @Bean
    public SSOTokenFilter ssoTokenFilter() {
        return new SSOTokenFilter();
    }

    @Bean
    public OAuth2StateWriter oAuth2StateWriter() {
        return new OAuth2StateWriter();
    }

}