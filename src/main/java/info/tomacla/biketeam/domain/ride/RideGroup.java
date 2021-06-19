package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.user.User;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride_group")
public class RideGroup {

    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Ride ride;
    private String name;
    @Column(name = "lower_speed")
    private double lowerSpeed;
    @Column(name = "upper_speed")
    private double upperSpeed;
    @Column(name = "map_id")
    private String mapId;
    @Column(name = "meeting_location")
    private String meetingLocation;
    @Column(name = "meeting_time")
    private LocalTime meetingTime;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "meeting_point_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "meeting_point_lng"))
    })
    @Embedded
    private Point meetingPoint;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ride_group_participant",
            joinColumns = @JoinColumn(name = "ride_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants;

    protected RideGroup() {

    }

    public RideGroup(String name,
                     double lowerSpeed,
                     double upperSpeed,
                     String mapId,
                     String meetingLocation,
                     LocalTime meetingTime,
                     Point meetingPoint,
                     Set<User> participants) {
        this.id = UUID.randomUUID().toString(); // tmp id
        setName(name);
        setLowerSpeed(lowerSpeed);
        setUpperSpeed(upperSpeed);
        setMapId(mapId);
        setMeetingLocation(meetingLocation);
        setMeetingTime(meetingTime);
        setMeetingPoint(meetingPoint);
        setParticipants(participants);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ride getRide() {
        return ride;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
    }

    public double getLowerSpeed() {
        return lowerSpeed;
    }

    public void setLowerSpeed(double lowerSpeed) {
        this.lowerSpeed = lowerSpeed;
    }

    public double getUpperSpeed() {
        return upperSpeed;
    }

    public void setUpperSpeed(double upperSpeed) {
        this.upperSpeed = upperSpeed;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = Strings.requireNonBlankOrNull(mapId);
    }

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = Strings.requireNonBlankOrNull(meetingLocation);
    }

    public LocalTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalTime meetingTime) {
        this.meetingTime = Objects.requireNonNull(meetingTime);
    }

    public Point getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Point meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public boolean hasParticipant(String userId) {
        return this.getParticipants().stream().map(User::getId).collect(Collectors.toSet()).contains(userId);
    }

    public void addParticipant(User participant) {
        this.getParticipants().add(participant);
    }

    public void removeParticipant(User participant) {
        this.getParticipants().removeIf(u -> u.getId().equals(participant.getId()));
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = Objects.requireNonNullElse(participants, new HashSet<>());
    }

    public void setRide(Ride ride, int index) {
        this.ride = Objects.requireNonNull(ride);
        this.id = ride.getId() + "-" + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideGroup rideGroup = (RideGroup) o;
        return id.equals(rideGroup.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}