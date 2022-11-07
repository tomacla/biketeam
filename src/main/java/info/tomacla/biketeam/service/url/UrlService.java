package info.tomacla.biketeam.service.url;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlService {

    @Value("${site.url}")
    private String siteUrl;

    public String getRideUrl(Team team, Ride ride) {
        return getTeamUrl(team) + "/rides/" + Optional.ofNullable(ride.getPermalink()).orElse(ride.getId());
    }

    public String getTripUrl(Team team, Trip trip) {
        return getTeamUrl(team) + "/trips/" + Optional.ofNullable(trip.getPermalink()).orElse(trip.getId());
    }

    public String getMapUrl(Team team, Map map) {
        return getTeamUrl(team) + "/maps/" + Optional.ofNullable(map.getPermalink()).orElse(map.getId());
    }

    public String getMapFitUrl(Team team, Map map) {
        return getTeamUrl(team) + "/maps/" + Optional.ofNullable(map.getPermalink()).orElse(map.getId()) + "/fit";
    }

    public String getMapGpxUrl(Team team, Map map) {
        return getTeamUrl(team) + "/maps/" + Optional.ofNullable(map.getPermalink()).orElse(map.getId()) + "/gpx";
    }

    public String getMapImageUrl(Team team, Map map) {
        return getTeamUrl(team) + "/maps/" + Optional.ofNullable(map.getPermalink()).orElse(map.getId()) + "/image";
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
        if (tmp.contains(":")) {
            tmp = tmp.substring(0, tmp.indexOf(':'));
        }
        return tmp;
    }
}
