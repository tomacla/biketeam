package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.message.RideMessage;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RideDTO {

    public String id;
    public String teamId;
    public String permalink;
    public PublishedStatus publishedStatus;
    public RideType type;
    public LocalDate date;
    public ZonedDateTime publishedAt;
    public String title;
    public String description;
    public boolean imaged;
    public List<RideGroupDTO> groups;
    public List<RideMessageDTO> messages;


    public static class RideMessageDTO {

        public String id;
        public MemberDTO author;
        public ZonedDateTime publishedAt;
        public String content;

        public static RideMessageDTO valueOf(RideMessage rideMessage) {

            if (rideMessage == null) {
                return null;
            }
            RideMessageDTO dto = new RideMessageDTO();
            dto.id = rideMessage.getId();
            dto.author = MemberDTO.valueOf(rideMessage.getUser());
            dto.publishedAt = rideMessage.getPublishedAt();
            dto.content = rideMessage.getContent();
            return dto;

        }

    }

    public static class RideGroupDTO {

        public String id;
        public String name;
        public double lowerSpeed;
        public double upperSpeed;
        public MapDTO map;
        public String meetingLocation;
        public LocalTime meetingTime;
        public PointDTO meetingPoint;
        public List<MemberDTO> participants;


        public static RideGroupDTO valueOf(RideGroup rideGroup) {

            if (rideGroup == null) {
                return null;
            }

            RideGroupDTO dto = new RideGroupDTO();
            dto.id = rideGroup.getId();
            dto.name = rideGroup.getName();
            dto.lowerSpeed = rideGroup.getLowerSpeed();
            dto.upperSpeed = rideGroup.getUpperSpeed();
            dto.map = MapDTO.valueOf(rideGroup.getMap());
            dto.meetingLocation = rideGroup.getMeetingLocation();
            dto.meetingTime = rideGroup.getMeetingTime();
            dto.meetingPoint = PointDTO.valueOf(rideGroup.getMeetingPoint());
            dto.participants = rideGroup.getParticipants().stream().map(MemberDTO::valueOf).collect(Collectors.toList());
            return dto;

        }

    }

    public static RideDTO valueOf(Ride ride) {

        if (ride == null) {
            return null;
        }

        RideDTO dto = new RideDTO();
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
        dto.messages = ride.getMessages().stream().map(RideMessageDTO::valueOf).collect(Collectors.toList());
        dto.groups = ride.getGroups().stream().map(RideGroupDTO::valueOf).collect(Collectors.toList());
        return dto;

    }

}
