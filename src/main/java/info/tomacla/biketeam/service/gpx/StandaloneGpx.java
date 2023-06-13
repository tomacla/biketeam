package info.tomacla.biketeam.service.gpx;

import java.util.List;
import java.util.Map;

public class StandaloneGpx {

    private final double length;
    private final double positiveElevation;
    private final double negativeElevation;
    private final String elevationProfile;
    private final String geojson;

    public StandaloneGpx(double length, double positiveElevation, double negativeElevation, String elevationProfile, String geojson) {
        this.length = length;
        this.positiveElevation = positiveElevation;
        this.negativeElevation = negativeElevation;
        this.elevationProfile = elevationProfile;
        this.geojson = geojson;
    }

    public double getLength() {
        return length;
    }

    public double getPositiveElevation() {
        return positiveElevation;
    }

    public double getNegativeElevation() {
        return negativeElevation;
    }

    public String getElevationProfile() {
        return elevationProfile;
    }

    public String getGeojson() {
        return geojson;
    }
}
