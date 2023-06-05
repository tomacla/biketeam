package info.tomacla.biketeam.service.garmin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * Service d'authentification sur Garmin
 */
@Service
public class GarminAuthService {

    @Value("${garmin.client.key}")
    private String clientKey;

    @Value("${garmin.client.secret}")
    private String clientSecret;

    @Value("${site.url}")
    private String siteUrl;

    @Autowired
    UserService userService;

    private OAuth10aService service;

    /**
     * Cache des requêtes de token
     */
    private final Cache<String, OAuth1RequestToken> requestTokens = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build();

    /**
     * Access tokens pour les utilisateurs non connectés
     */
    private final Cache<String, GarminToken> accessTokens = Caffeine.newBuilder().expireAfterAccess(Duration.ofDays(365)).build();

    @PostConstruct
    protected void init() {
        service = new ServiceBuilder(clientKey)
                .apiSecret(clientSecret)
                .callback(siteUrl + "/auth/garmin")
                .build(new GarminOAuthApi());
    }

    public GarminToken queryToken(HttpServletRequest request, HttpServletResponse response) throws GarminAuthException {
        return queryToken(request, response, true);
    }

    /**
     * Récupère un token si possible
     *
     * @param request
     * @param response
     * @return null si l'utilisateur n'est pas authentifié, mais il a été redirigé vers Garmin
     * @throws GarminAuthException
     */
    protected GarminToken queryToken(HttpServletRequest request, HttpServletResponse response, boolean saveUrl) throws GarminAuthException {

        // utilisateur connecté
        String userId = getUserId();

        if (userId == null) {
            // l'utilisateur n'est pas connecté, retrouve l'access token depuis le cookie garmin_token
            String garminToken = getCookieValue(request, "garmin_token");
            if (garminToken != null) {
                // recherche l'access token en cache
                GarminToken token = accessTokens.getIfPresent(garminToken);
                if (token != null) {
                    return token;
                }
            }
        } else {
            // recherche le token depuis la base
            GarminToken dbGarminToken = getGarminTokenForUserId(userId);
            if (dbGarminToken != null) {
                return dbGarminToken;
            }
        }

        // à partir d'ici l'utilisateur n'est pas connecté sur Garmin, il va être redirigé

        // génération d'un request token
        OAuth1RequestToken requestToken;
        try {
            requestToken = service.getRequestToken();
        } catch (Exception e) {
            throw new GarminAuthException(e);
        }
        // mise en cache
        requestTokens.put(requestToken.getToken(), requestToken);

        // sauvegarde de l'url actuelle
        if (saveUrl) {
            String url;
            if (request.getQueryString() != null) {
                url = request.getRequestURI() + "?" + request.getQueryString();
            } else {
                url = request.getRequestURI();
            }
            response.addCookie(createCookie("garmin_url", url));
        }
        // permettra de valider le retour de Garmin
        response.addCookie(createCookie("garmin_token", requestToken.getToken()));

        // URL de connexion sur Garmin
        String authorizationUrl = service.getAuthorizationUrl(requestToken);
        try {
            // redirige vers Garmin
            response.sendRedirect(authorizationUrl);
        } catch (IOException e) {
            throw new GarminAuthException(e);
        }
        // pas de token mais redirection vers Garmin
        return null;
    }

    protected String getUserId() {
        // récupère le user id
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().contains(Authorities.user())) {
            if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
                if (principal.getAttribute("id") instanceof String id) {
                    return id;
                }
            }
        }
        return null;
    }

    protected GarminToken getGarminTokenForUserId(String userId) {
        // récupère le token garmin depuis la base
        Optional<User> optionalUser = userService.get(userId);
        if (optionalUser.isPresent()) {
            String dbGarminToken = optionalUser.get().getGarminToken();
            String dbGarminTokenSecret = optionalUser.get().getGarminTokenSecret();
            if (dbGarminToken != null &&
                    dbGarminTokenSecret != null) {
                return new GarminToken(
                        userId,
                        new OAuth1AccessToken(
                                dbGarminToken,
                                dbGarminTokenSecret
                        )
                );
            }
        }
        return null;
    }

    public void auth(HttpServletRequest request, HttpServletResponse response, String oauthToken, String oauthVerifier) throws GarminAuthException {
        // récupère le request token
        OAuth1RequestToken requestToken = requestTokens.getIfPresent(oauthToken);
        if (requestToken == null) {
            // pas de request token, on relance une authentification
            queryToken(request, response, false);
            return;
        }
        // récupère l'access token depuis Garmin
        OAuth1AccessToken accessToken;
        try {
            accessToken = service.getAccessToken(requestToken, oauthVerifier);
        } catch (Exception e) {
            throw new GarminAuthException(e);
        }

        String userId = getUserId();
        if (userId == null) {
            // pas connecté, on stocke l'access token dans le cache
            accessTokens.put(oauthToken, new GarminToken(null, accessToken));
        } else {
            // connecté, on stocke le token en base
            updateGarminToken(userId, accessToken);
        }
        // récupération de l'url sauvegardée
        String garminUrl = getCookieValue(request, "garmin_url");
        if (garminUrl != null) {
            try {
                // redirection
                response.sendRedirect(garminUrl);
            } catch (IOException e) {
                throw new GarminAuthException(e);
            }
        }
    }

    protected void updateGarminToken(String userId, OAuth1AccessToken accessToken) {
        Optional<User> optionalUser = userService.get(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (accessToken == null) {
                user.setGarminToken(null);
                user.setGarminTokenSecret(null);
            } else {
                user.setGarminToken(accessToken.getToken());
                user.setGarminTokenSecret(accessToken.getTokenSecret());
            }
            userService.save(user);
        }
    }

    String execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, GarminToken garminToken, OAuthRequest oAuthRequest) throws GarminAuthException {
        try (Response garminResponse = doExecute(garminToken, oAuthRequest)) {
            // non authentifié/none authorisé
            if (garminResponse.getCode() == 401 || garminResponse.getCode() == 403) {
                if (garminToken.userId() != null) {
                    // suppression de l'access token en base
                    updateGarminToken(garminToken.userId(), null);
                } else {
                    // FIXME supprimer l'access token du cache
                }
                queryToken(httpServletRequest, httpServletResponse, false);
                return null;
            } else {
                return garminResponse.getBody();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response doExecute(GarminToken garminToken, OAuthRequest request) {
        Response response;
        try {
            // signe la requête avant de l'envoyer à Garmin
            service.signRequest(garminToken.accessToken(), request);
            // exécution de la requête
            response = service.execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    protected Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        // cookie valide dès la racine
        cookie.setPath("/");
        return cookie;
    }

    protected String getCookieValue(HttpServletRequest request, String cookieName) {
        String garminToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                garminToken = cookie.getValue();
            }
        }
        return garminToken;
    }

}
