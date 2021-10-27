package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.team.Team;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Value("${site.url}")
    private String siteUrl;

    public String getRideUrl(Team team, String mapId) {
        return getTeamUrl(team) + "/rides/" + mapId;
    }

    public String getTripUrl(Team team, String tripId) {
        return getTeamUrl(team) + "/trips/" + tripId;
    }

    public String getMapUrl(Team team, String mapId) {
        return getTeamUrl(team) + "/maps/" + mapId;
    }

    public String getMapFitUrl(Team team, String mapId) {
        return getTeamUrl(team) + "/maps/" + mapId + "/fit";
    }

    public String getMapGpxUrl(Team team, String mapId) {
        return getTeamUrl(team) + "/maps/" + mapId + "/gpx";
    }

    public String getMapImageUrl(Team team, String mapId) {
        return getTeamUrl(team) + "/maps/" + mapId + "/image";
    }

    public String getUrlWithSuffix(String suffix) {
        return siteUrl + suffix;
    }

    public String getTeamUrl(Team team) {
        if (team.getConfiguration().isDomainConfigured()) {
            return team.getConfiguration().getDomain();
        }
        return siteUrl + "/" + team.getId();
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getCookieDomain() {
        String tmp = siteUrl.replace("https://", "");
        tmp = tmp.replace("http://", "");
        return tmp;
    }
}
