package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.user.User;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride_group")
public class RideGroup {

    @Id
    @UuidGenerator
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ride_id")
    private Ride ride;
    private String name;
    @Column(name = "average_speed")
    private double averageSpeed;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id")
    private Map map;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ride_group_participant",
            joinColumns = @JoinColumn(name = "ride_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is blank");
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }


    public LocalTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalTime meetingTime) {
        this.meetingTime = Objects.requireNonNull(meetingTime, "meetingTime is null");
    }


    public boolean hasParticipant(String userId) {
        return this.getParticipants().stream().map(User::getId).anyMatch(uid -> uid.equals(userId));
    }

    public void addParticipant(User participant) {
        this.getParticipants().add(participant);
    }

    public void removeParticipant(User participant) {
        this.getParticipants().remove(participant);
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public List<User> getSortedParticipants() {
        return participants.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    public void setParticipants(Set<User> participants) {
        this.participants = Objects.requireNonNullElse(participants, new HashSet<>());
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