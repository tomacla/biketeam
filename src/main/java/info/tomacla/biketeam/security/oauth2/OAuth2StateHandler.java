package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class OAuth2StateHandler {

    private final StringKeyGenerator generator = new Base64StringKeyGenerator(Base64.getUrlEncoder());

    public static final String SEPARATOR = ",";

    private UrlService urlService;
    private TeamService teamService;

    public OAuth2StateHandler(UrlService urlService, TeamService teamService) {
        this.urlService = urlService;
        this.teamService = teamService;
    }

    public String generateState() {
        return generator.generateKey();
    }

    public String generateState(String... elements) {
        return String.join(SEPARATOR, elements);
    }

    public String generateState(HttpServletRequest currentHttpRequest) {

        if (currentHttpRequest == null) {
            return generateState();
        }

        final String customRequestUri = getCustomRequestUri(currentHttpRequest);
        String redirect = null;
        if(customRequestUri != null) {
            if (isAbsoluteUri(customRequestUri)) {
                redirect = customRequestUri;
            } else if (!customRequestUri.equals("/login")) {
                String teamId = extractTeamId(customRequestUri);
                if (!ObjectUtils.isEmpty(teamId)) {
                    final Optional<Team> team = teamService.get(teamId);
                    if (team.isPresent()) {
                        String resultUri = extractResulting(customRequestUri);
                        final String teamUrl = urlService.getTeamUrl(team.get());
                        redirect = teamUrl + resultUri;
                    } else {
                        redirect = urlService.getUrlWithSuffix(customRequestUri);
                    }
                }
            }
        }

        if (ObjectUtils.isEmpty(redirect)) {
            return generateState();
        }

        return generateState(generateState(), redirect);

    }


    private String extractTeamId(String uri) {
        int startIndex = uri.startsWith("/") ? 1 : 0;
        final int nextSlash = uri.indexOf('/', startIndex);
        if (nextSlash == -1) {
            return uri.substring(startIndex);
        }
        return uri.substring(startIndex, nextSlash);
    }

    private String extractResulting(String uri) {
        int firstIndex = uri.startsWith("/") ? 1 : 0;
        int startIndex = uri.indexOf('/', firstIndex);
        if (startIndex == -1) {
            return "";
        }
        return uri.substring(startIndex);
    }

    private String getCustomRequestUri(HttpServletRequest request) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .query(request.getQueryString())
                .build();

        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String requestUri = queryParams.getFirst("requestUri");
        if (requestUri != null) {
            return URLDecoder.decode(requestUri, StandardCharsets.UTF_8);
        }
        return null;
    }

    public boolean isAbsoluteUri(String uri) {
        return uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://");
    }

    public String decodeState(String stateDecoded) {

        if (stateDecoded == null) {
            return null;
        }

        String[] split = stateDecoded.split(SEPARATOR);
        if (split.length != 2) {
            return null;
        }

        return split[1];

    }
}
