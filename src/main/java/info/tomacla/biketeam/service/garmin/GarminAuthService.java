package info.tomacla.biketeam.service.garmin;

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

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Service d'authentification sur Garmin
 */
@Service
public class GarminAuthService {

    public static final String GARMIN_ACCESS_TOKEN = "garmin_access_token";
    public static final String GARMIN_REQUEST_TOKEN = "garmin_request_token";
    public static final String GARMIN_URL = "garmin_url";
    @Value("${garmin.client.key}")
    private String clientKey;

    @Value("${garmin.client.secret}")
    private String clientSecret;

    @Value("${site.url}")
    private String siteUrl;

    @Autowired
    UserService userService;

    private OAuth10aService service;

    @PostConstruct
    protected void init() {
        service = new ServiceBuilder(clientKey)
                .apiSecret(clientSecret)
                .callback(siteUrl + "/auth/garmin")
                .build(new GarminOAuthApi());
    }

    public GarminToken queryToken(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession) throws GarminAuthException {
        return queryToken(request, response, httpSession, true);
    }

    /**
     * Récupère un token si possible
     *
     * @param request
     * @param response
     * @return null si l'utilisateur n'est pas authentifié, mais il a été redirigé vers Garmin
     * @throws GarminAuthException
     */
    protected GarminToken queryToken(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession, boolean saveUrl) throws GarminAuthException {

        // utilisateur connecté
        String userId = getUserId();

        if (userId == null) {
            // recherche l'access token en session
            GarminToken token = (GarminToken) httpSession.getAttribute(GARMIN_ACCESS_TOKEN);
            if (token != null) {
                return token;
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
        httpSession.setAttribute(GARMIN_REQUEST_TOKEN, requestToken);

        // sauvegarde de l'url actuelle
        if (saveUrl) {
            String url;
            if (request.getQueryString() != null) {
                url = request.getRequestURI() + "?" + request.getQueryString();
            } else {
                url = request.getRequestURI();
            }
            httpSession.setAttribute(GARMIN_URL, url);
        }

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

    public void auth(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession, String oauthToken, String oauthVerifier) throws GarminAuthException {
        // récupère le request token
        OAuth1RequestToken requestToken = (OAuth1RequestToken) httpSession.getAttribute(GARMIN_REQUEST_TOKEN);
        if (requestToken == null || !Objects.equals(requestToken.getToken(), oauthToken)) {
            // pas de request token, on relance une authentification
            queryToken(request, response, httpSession, false);
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
            httpSession.setAttribute(GARMIN_ACCESS_TOKEN, new GarminToken(null, accessToken));
        } else {
            // connecté, on stocke le token en base
            updateGarminToken(userId, accessToken);
        }
        // récupération de l'url sauvegardée
        String garminUrl = (String) httpSession.getAttribute(GARMIN_URL);
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

    String execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession, GarminToken garminToken, OAuthRequest oAuthRequest) throws GarminAuthException {
        try (Response garminResponse = doExecute(garminToken, oAuthRequest)) {
            // non authentifié/none authorisé
            if (garminResponse.getCode() == 401 || garminResponse.getCode() == 403) {
                if (garminToken.userId() != null) {
                    // suppression de l'access token en base
                    updateGarminToken(garminToken.userId(), null);
                } else {
                    httpSession.removeAttribute(GARMIN_ACCESS_TOKEN);
                }
                queryToken(httpServletRequest, httpServletResponse, httpSession, false);
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

}
