package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.place.Place;

public class PlaceDTO {

    public String id;
    public String teamId;
    public String name;
    public String address;
    public String link;
    public PointDTO point;

    public static PlaceDTO valueOf(Place place) {

        if (place == null) {
            return null;
        }
        PlaceDTO dto = new PlaceDTO();
        dto.id = place.getId();
        dto.teamId = place.getTeamId();
        dto.name = place.getName();
        dto.address = place.getAddress();
        dto.link = place.getLink();
        ;
        if (place.getPoint() != null) {
            dto.point = PointDTO.valueOf(place.getPoint());
        }
        return dto;

    }

}
