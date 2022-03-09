package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapDTO {

    public String id;
    public String teamId;
    public String permalink;
    public String name;
    public double length;
    public MapType type;
    public double positiveElevation;
    public double negativeElevation;
    public LocalDate postedAt;
    public List<String> tags;
    public boolean crossing;

    public static MapDTO valueOf(Map map) {

        if(map == null) {
            return null;
        }

        MapDTO dto = new MapDTO();
        dto.id = map.getId();
        dto.teamId = map.getTeamId();
        dto.permalink = map.getPermalink();
        dto.name = map.getName();
        dto.length = map.getLength();
        dto.type = map.getType();
        dto.positiveElevation = map.getPositiveElevation();
        dto.negativeElevation = map.getNegativeElevation();
        dto.postedAt = map.getPostedAt();
        dto.tags = map.getTags();
        dto.crossing = map.isCrossing();
        return dto;

    }


}
