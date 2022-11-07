package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.feed.FeedType;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class PartialRideDTO {

    public FeedType feedType;
    public String id;
    public String teamId;
    public String permalink;
    public PublishedStatus publishedStatus;
    public RideType type;
    public LocalDate date;
    public PlaceDTO startPlace;
    public PlaceDTO endPlace;
    public LocalTime meetingTime;
    public ZonedDateTime publishedAt;
    public String title;
    public String description;
    public boolean imaged;
    public int numberOfGroups;


    public static PartialRideDTO valueOf(Ride ride) {

        if (ride == null) {
            return null;
        }

        PartialRideDTO dto = new PartialRideDTO();
        dto.feedType = ride.getFeedType();
        dto.id = ride.getId();
        dto.teamId = ride.getTeamId();
        dto.permalink = ride.getPermalink();
        dto.publishedStatus = ride.getPublishedStatus();
        dto.type = ride.getType();
        dto.date = ride.getDate();
        dto.publishedAt = ride.getPublishedAt();
        dto.title = ride.getTitle();
        dto.description = ride.getDescription();
        dto.imaged = ride.isImaged();
        dto.numberOfGroups = ride.getGroups().size();
        if (!ride.getGroups().isEmpty()) {
            dto.meetingTime = ride.getSortedGroups().get(0).getMeetingTime();
        }
        if (ride.getStartPlace() != null) {
            dto.startPlace = PlaceDTO.valueOf(ride.getStartPlace());
        }
        if (ride.getEndPlace() != null) {
            dto.endPlace = PlaceDTO.valueOf(ride.getEndPlace());
        }
        return dto;

    }

}
