package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripStage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TripDTO {

    public String id;
    public String teamId;
    public String permalink;
    public LocalDate startDate;
    public LocalDate endDate;
    public double lowerSpeed;
    public double upperSpeed;
    public LocalTime meetingTime;
    public MapType type;
    public PublishedStatus publishedStatus;
    public ZonedDateTime publishedAt;
    public String title;
    public String description;
    public boolean imaged;

    public List<TripStageDTO> stages;
    public List<MemberDTO> participants;

    public static class TripStageDTO {

        public String id;
        public LocalDate date;
        public String name;
        public MapDTO map;

        public static TripStageDTO valueOf(TripStage tripStage) {

            if (tripStage == null) {
                return null;
            }

            TripStageDTO dto = new TripStageDTO();
            dto.id = tripStage.getId();
            dto.date = tripStage.getDate();
            dto.name = tripStage.getName();
            dto.map = MapDTO.valueOf(tripStage.getMap());
            return dto;

        }

    }

    public static TripDTO valueOf(Trip trip) {

        if (trip == null) {
            return null;
        }

        TripDTO dto = new TripDTO();
        dto.id = trip.getId();
        dto.teamId = trip.getTeamId();
        dto.permalink = trip.getPermalink();
        dto.startDate = trip.getStartDate();
        dto.endDate = trip.getEndDate();
        dto.lowerSpeed = trip.getLowerSpeed();
        dto.upperSpeed = trip.getUpperSpeed();
        dto.meetingTime = trip.getMeetingTime();
        dto.type = trip.getType();
        dto.publishedStatus = trip.getPublishedStatus();
        dto.publishedAt = trip.getPublishedAt();
        dto.title = trip.getTitle();
        dto.description = trip.getDescription();
        dto.imaged = trip.isImaged();

        dto.stages = trip.getStages().stream().map(TripStageDTO::valueOf).collect(Collectors.toList());
        dto.participants = trip.getParticipants().stream().map(MemberDTO::valueOf).collect(Collectors.toList());

        return dto;
    }

}
