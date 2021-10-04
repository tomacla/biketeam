package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class OAuth2StateWriter extends Base64StringKeyGenerator {

    private final StringKeyGenerator generator = new Base64StringKeyGenerator(Base64.getUrlEncoder());

    @Autowired
    private UrlService urlService;

    @Autowired
    private TeamService teamService;

    @Override
    public String generateKey() {
        HttpServletRequest currentHttpRequest = getCurrentHttpRequest();
        if (currentHttpRequest != null) {
            final String customRequestUri = getCustomRequestUri(currentHttpRequest);
            String redirect = null;
            if (customRequestUri != null) {
                String teamId = extractTeamId(customRequestUri);
                String resultUri = extractResulting(customRequestUri);
                if (!ObjectUtils.isEmpty(teamId)) {
                    final Optional<Team> team = teamService.get(teamId);
                    if (team.isPresent()) {
                        final String teamUrl = urlService.getTeamUrl(team.get());
                        redirect = teamUrl + resultUri;
                    }
                }
            }

            if (!ObjectUtils.isEmpty(redirect)) {
                return generator.generateKey()
                        + OAuth2SuccessHandler.SEPARATOR
                        + redirect;
            }

        }
        return generator.generateKey();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
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

}