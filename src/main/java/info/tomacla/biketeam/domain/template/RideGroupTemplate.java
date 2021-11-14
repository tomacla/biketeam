package info.tomacla.biketeam.domain.template;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "ride_group_template")
public class RideGroupTemplate {

    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ride_template_id")
    private RideTemplate rideTemplate;
    private String name;
    @Column(name = "lower_speed")
    private double lowerSpeed;
    @Column(name = "upper_speed")
    private double upperSpeed;
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

    public RideGroupTemplate() {
    }

    public RideGroupTemplate(String name,
                             double lowerSpeed,
                             double upperSpeed,
                             String meetingLocation,
                             LocalTime meetingTime,
                             Point meetingPoint) {
        setName(name);
        setLowerSpeed(lowerSpeed);
        setUpperSpeed(upperSpeed);
        setMeetingLocation(meetingLocation);
        setMeetingTime(meetingTime);
        setMeetingPoint(meetingPoint);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RideTemplate getRideTemplate() {
        return rideTemplate;
    }

    public void setRideTemplate(RideTemplate rideTemplate, int index) {
        this.rideTemplate = Objects.requireNonNull(rideTemplate);
        this.id = rideTemplate.getId() + "-" + index;
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
