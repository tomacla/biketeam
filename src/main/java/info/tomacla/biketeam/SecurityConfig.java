package info.tomacla.biketeam;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.security.completion.AccountCompletionFilter;
import info.tomacla.biketeam.security.login.CustomAccessDeniedHandler;
import info.tomacla.biketeam.security.login.CustomLoginUrlAuthenticationEntryPoint;
import info.tomacla.biketeam.security.oauth2.link.OAuth2LoginFailureHandler;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationFilter;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationProvider;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationService;
import info.tomacla.biketeam.security.passkey.PasskeyLoginFailureHandler;
import info.tomacla.biketeam.security.passkey.PasskeyLoginSuccessHandler;
import info.tomacla.biketeam.security.password.EmailPasswordAuthenticationProvider;
import info.tomacla.biketeam.security.password.LoginFailureHandler;
import info.tomacla.biketeam.security.session.CustomSessionIdResolver;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.PostgreSqlJdbcIndexedSessionRepositoryCustomizer;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  LoginFailureHandler loginFailureHandler,
                                                  OAuth2LoginFailureHandler oauth2LoginFailureHandler,
                                                  AccountCompletionFilter accountCompletionFilter,
                                                  PasskeyAuthenticationFilter passkeyAuthenticationFilter,
                                                  RememberMeServices rememberMeServices) throws Exception {

        // expression with access to all beans in context (userService, ...)
        SecurityExpressionHandler<RequestAuthorizationContext> expressionHandler = getExpressionHandler();

        // global conf
        http.cors(Customizer.withDefaults());

        // CSRF : la protection reste desactivee sur l'existant (API, formulaires historiques) mais
        // est OBLIGATOIRE sur les nouvelles routes d'authentification : sans elle, un POST
        // interdomaine pourrait poser un mot de passe sur le compte d'un utilisateur connecte.
        http.csrf(c -> c.requireCsrfProtectionMatcher(new OrRequestMatcher(
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/login"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/register"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/register/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/forgot-password"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/reset-password"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/account/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/password"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/email"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/email/resend"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/unlink/*"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/passkeys"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/passkeys/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/login/webauthn"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/login/webauthn/options"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/users/me/delete"))));

        // requests conf
        http.authorizeHttpRequests(auth -> {

            // request matchers is using old style ant_path_matcher (see application.properties)

            // pages d'erreur : le forward vers /error matcherait sinon /{teamId}/** (teamId = "error")
            auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
            auth.requestMatchers("/error").permitAll();

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
            // attention : ces routes doivent rester declarees AVANT /{teamId}/** car le pattern
            // ant /{teamId}/** matche aussi une URL a un seul segment (/register par exemple)
            auth.requestMatchers("/teams", "/notifications/**", "/autocomplete/**", "/users/*/image",
                    "/catalog/**", "/confirm-email",
                    "/register/**", "/verify-email", "/forgot-password", "/reset-password",
                    "/forms/token").permitAll();

            // web protected endpoints
            auth.requestMatchers("/account/**").authenticated();
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
        // les services sont declares en bean plutot que configures ici : le filtre passkey doit
        // poser le MEME cookie que les autres modes de connexion, et pour cela partager
        // l'instance. Les parametres sont identiques a ceux de la configuration precedente.
        http.rememberMe(rm -> {
            rm.rememberMeServices(rememberMeServices);
            rm.userDetailsService(userDetailsService);
            rm.key(rememberMeKey);
        });

        // oauth2 conf
        // le failure handler intercepte le code account_link_conflict pour ouvrir l'ecran de
        // confirmation de fusion ; le bean Oauth2AuthUserService reste decouvert automatiquement
        http.oauth2Login(o -> o.failureHandler(oauth2LoginFailureHandler));

        // form login (email + mot de passe)
        http.formLogin(f -> f
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler())
                .failureHandler(loginFailureHandler));
        // le bean EmailPasswordAuthenticationProvider est deja enregistre dans l'AuthenticationManager
        // global (parent de celui de cette chaine) : ne pas l'ajouter ici aussi, sinon chaque echec de
        // connexion est evalue deux fois (local puis parent) et compte double dans LoginAttemptService

        // depot de contexte explicite : depuis Spring Security 6, le contexte modifie en cours
        // de requete n'est plus sauvegarde automatiquement
        http.securityContext(sc -> sc.securityContextRepository(securityContextRepository()));

        // logout
        // OBLIGATOIRE : des qu'un CsrfConfigurer est present dans la chaine, LogoutConfigurer
        // restreint /logout au POST. Or la navbar utilise un lien GET et UserController.deleteMyself
        // redirige vers /logout : sans ce matcher, plus personne ne peut se deconnecter.
        http.logout(logout -> logout
                .logoutRequestMatcher(new OrRequestMatcher(
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/logout"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/logout")))
                .logoutSuccessUrl("/"));

        // connexion par passkey : POST /login/webauthn.
        // Place avant UsernamePasswordAuthenticationFilter par symetrie avec form login ; les deux
        // filtres ne se marchent pas dessus, leurs matchers portent sur des URL distinctes.
        http.addFilterBefore(passkeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // completion de compte forcee
        // place APRES l'AuthorizationFilter : les regles d'autorisation sont deja appliquees,
        // une redirection 302 ne peut donc pas masquer un 401 ou un 403.
        http.addFilterAfter(accountCompletionFilter, AuthorizationFilter.class);

        return http.build();
    }

    private AuthenticationSuccessHandler loginSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        return handler;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Tout bean de type Filter est sinon enregistre automatiquement par Spring Boot dans le
     * conteneur de servlets, en plus de son ajout explicite a la chaine de securite. Comme
     * OncePerRequestFilter se protege d'une double execution par un attribut de requete porte
     * par le NOM DU BEAN, la premiere des deux invocations neutraliserait la seconde.
     * L'enregistrement conteneur est donc desactive : le filtre ne s'execute que dans la chaine
     * de securite, la ou l'authentification est disponible.
     */
    @Bean
    public FilterRegistrationBean<AccountCompletionFilter> accountCompletionFilterRegistration(
            AccountCompletionFilter accountCompletionFilter) {
        FilterRegistrationBean<AccountCompletionFilter> registration = new FilterRegistrationBean<>(accountCompletionFilter);
        registration.setEnabled(false);
        return registration;
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

    /**
     * Ecriture des attributs de session en UPSERT (ON CONFLICT DO UPDATE) au lieu d'un INSERT sec.
     * <p>
     * Deux requetes concurrentes d'une meme session (une page et ses appels /notifications,
     * /autocomplete...) chargent chacune leur copie de la session : si toutes deux posent un
     * attribut pour la premiere fois, elles le voient toutes deux comme nouveau et l'INSERT de la
     * seconde violait la cle primaire de SPRING_SESSION_ATTRIBUTES (DuplicateKeyException remontee
     * en 500, par rafales, a chaque connexion).
     * <p>
     * Le customizer est fourni par spring-session-jdbc mais n'est pas enregistre automatiquement.
     * Il doit passer APRES celui de Spring Boot, qui appelle setTableName() - ce qui reinitialise
     * toutes les requetes : c'est le cas, celui de Spring Boot est @Order(HIGHEST_PRECEDENCE) et
     * ce bean, sans ordre, est applique en dernier.
     */
    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> postgreSqlSessionRepositoryCustomizer() {
        return new PostgreSqlJdbcIndexedSessionRepositoryCustomizer();
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

    /**
     * Services remember-me partages entre le form login, la connexion OAuth2 et la connexion par
     * passkey. La signature du cookie repose sur {@code UserDetails.getPassword()}, c'est-a-dire
     * sur user_account.auth_token_seed (voir OAuth2UserDetails) : aucun mot de passe n'y transite.
     */
    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(rememberMeKey, userDetailsService,
                TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256);
        services.setAlwaysRemember(true);
        services.setTokenValiditySeconds(rememberMeValidity);
        return services;
    }

    @Bean
    public PasskeyAuthenticationFilter passkeyAuthenticationFilter(AuthenticationManager authenticationManager,
                                                                   PasskeyAuthenticationService passkeyAuthenticationService,
                                                                   RememberMeServices rememberMeServices,
                                                                   ObjectMapper objectMapper) {
        PasskeyAuthenticationFilter filter = new PasskeyAuthenticationFilter(
                authenticationManager, passkeyAuthenticationService, objectMapper);
        filter.setSecurityContextRepository(securityContextRepository());
        filter.setRememberMeServices(rememberMeServices);
        filter.setAuthenticationSuccessHandler(new PasskeyLoginSuccessHandler(objectMapper, "/"));
        filter.setAuthenticationFailureHandler(new PasskeyLoginFailureHandler(objectMapper));
        return filter;
    }

    /**
     * Meme motif que pour AccountCompletionFilter : sans cela Spring Boot enregistrerait aussi le
     * filtre dans le conteneur de servlets, ou il s'executerait AVANT la chaine de securite et
     * tenterait une premiere authentification hors contexte.
     */
    @Bean
    public FilterRegistrationBean<PasskeyAuthenticationFilter> passkeyAuthenticationFilterRegistration(
            PasskeyAuthenticationFilter passkeyAuthenticationFilter) {
        FilterRegistrationBean<PasskeyAuthenticationFilter> registration =
                new FilterRegistrationBean<>(passkeyAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Enregistrement EXPLICITE des providers dans l'AuthenticationManager global.
     * <p>
     * Indispensable des lors qu'il existe plus d'un bean de type AuthenticationProvider :
     * InitializeAuthenticationProviderBeanManagerConfigurer, qui enregistrait jusqu'ici
     * EmailPasswordAuthenticationProvider tout seul, ne le fait QUE s'il trouve exactement un
     * bean de ce type. L'ajout de PasskeyAuthenticationProvider aurait donc, sans ce bean,
     * silencieusement desactive la connexion par email et mot de passe.
     */
    @Bean
    public GlobalAuthenticationConfigurerAdapter authenticationProvidersConfigurer(
            EmailPasswordAuthenticationProvider emailPasswordAuthenticationProvider,
            PasskeyAuthenticationProvider passkeyAuthenticationProvider) {
        return new GlobalAuthenticationConfigurerAdapter() {
            @Override
            public void init(AuthenticationManagerBuilder auth) {
                auth.authenticationProvider(emailPasswordAuthenticationProvider);
                auth.authenticationProvider(passkeyAuthenticationProvider);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
