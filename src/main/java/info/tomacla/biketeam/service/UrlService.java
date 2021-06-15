package info.tomacla.biketeam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Value("${site.url}")
    private String siteUrl;

    public String getRideUrl(String teamId, String mapId) {
        return siteUrl + "/" + teamId + "/rides/" + mapId;
    }

    public String getMapUrl(String teamId, String mapId) {
        return siteUrl + "/" + teamId + "/maps/" + mapId;
    }

    public String getMapFitUrl(String teamId, String mapId) {
        return siteUrl + "/api/" + teamId + "/maps/" + mapId + "/fit";
    }

    public String getMapGpxUrl(String teamId, String mapId) {
        return siteUrl + "/api/" + teamId + "/maps/" + mapId + "/gpx";
    }

    public String getMapImageUrl(String teamId, String mapId) {
        return siteUrl + "/api/" + teamId + "maps/" + mapId + "/image";
    }

    public String getUrlWithSuffix(String suffix) {
        return siteUrl + suffix;
    }

    public String getUrl() {
        return siteUrl;
    }


}
