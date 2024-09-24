package info.tomacla.biketeam.domain.template;

import info.tomacla.biketeam.common.datatype.Strings;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ride_group_template")
public class RideGroupTemplate {

    @Id
    private String id = UUID.randomUUID().toString();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ride_template_id")
    private RideTemplate rideTemplate;
    private String name;
    @Column(name = "average_speed")
    private double averageSpeed;
    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public RideTemplate getRideTemplate() {
        return rideTemplate;
    }

    public void setRideTemplate(RideTemplate rideTemplate) {
        this.rideTemplate = rideTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
    }


    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }


    public LocalTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalTime meetingTime) {
        this.meetingTime = Objects.requireNonNull(meetingTime, "meetingTime is null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideGroupTemplate rideGroupTemplate = (RideGroupTemplate) o;
        return id.equals(rideGroupTemplate.id) && rideTemplate.equals(rideGroupTemplate.rideTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rideTemplate);
    }

}
