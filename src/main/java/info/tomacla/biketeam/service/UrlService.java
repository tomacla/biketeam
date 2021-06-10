package info.tomacla.biketeam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Value("${site.url}")
    private String siteUrl;

    public String getRideUrl(String mapId) {
        return siteUrl + "/rides/" + mapId;
    }

    public String getMapUrl(String mapId) {
        return siteUrl + "/maps/" + mapId;
    }

    public String getMapFitUrl(String mapId) {
        return siteUrl + "/api/maps/" + mapId + "/fit";
    }

    public String getMapGpxUrl(String mapId) {
        return siteUrl + "/api/maps/" + mapId + "/gpx";
    }

    public String getMapImageUrl(String mapId) {
        return siteUrl + "/api/maps/" + mapId + "/image";
    }

    public String getUrlWithSuffix(String suffix) {
        return siteUrl + suffix;
    }

    public String getUrl() {
        return siteUrl;
    }


}
