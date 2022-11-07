package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.feed.FeedType;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.trip.Trip;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class PartialTripDTO {

    public FeedType feedType;
    public String id;
    public String teamId;
    public String permalink;
    public LocalDate startDate;
    public LocalDate endDate;
    public PlaceDTO startPlace;
    public PlaceDTO endPlace;
    public double lowerSpeed;
    public double upperSpeed;
    public LocalTime meetingTime;
    public MapType type;
    public PublishedStatus publishedStatus;
    public ZonedDateTime publishedAt;
    public String title;
    public String description;
    public boolean imaged;
    public int numberOfStages;

    public static PartialTripDTO valueOf(Trip trip) {

        if (trip == null) {
            return null;
        }

        PartialTripDTO dto = new PartialTripDTO();
        dto.feedType = trip.getFeedType();
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
        dto.numberOfStages = trip.getStages().size();
        if (trip.getStartPlace() != null) {
            dto.startPlace = PlaceDTO.valueOf(trip.getStartPlace());
        }
        if (trip.getEndPlace() != null) {
            dto.endPlace = PlaceDTO.valueOf(trip.getEndPlace());
        }

        return dto;
    }

}
