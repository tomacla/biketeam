package info.tomacla.biketeam.service.webhook;

import info.tomacla.biketeam.domain.place.Place;

public class PlaceWebhookDTO {

    public String name;
    public String address;
    public String link;
    public PointWebhookDTO point;

    public static PlaceWebhookDTO valueOf(Place place) {
        if (place == null) {
            return null;
        }
        PlaceWebhookDTO dto = new PlaceWebhookDTO();
        dto.name = place.getName();
        dto.address = place.getAddress();
        dto.link = place.getLink();
        dto.point = PointWebhookDTO.valueOf(place.getPoint());
        return dto;
    }

}
