package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.ride.Ride;

public class PointDTO {

    public double lat;
    public double lng;

    public static PointDTO valueOf(Point point) {

        if(point == null) {
            return null;
        }

        PointDTO dto = new PointDTO();
        dto.lat = point.getLat();
        dto.lng = point.getLng();
        return dto;
    }

}
