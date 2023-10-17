package info.tomacla.biketeam.service.webhook;

import info.tomacla.biketeam.common.geo.Point;

public class PointWebhookDTO {

    public double lat;
    public double lng;

    public static PointWebhookDTO valueOf(Point point) {
        if (point == null) {
            return null;
        }
        PointWebhookDTO dto = new PointWebhookDTO();
        dto.lat = point.getLat();
        dto.lng = point.getLng();
        return dto;

    }


}
