package info.tomacla.biketeam.domain.place;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "place")
public class Place {

    @Id
    private String id = UUID.randomUUID().toString();

    private String teamId;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "link")
    private String link;

    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "point_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "point_lng"))
    })
    @Embedded
    private Point point;
    @Column(name = "start_place")
    private boolean startPlace;
    @Column(name = "end_place")
    private boolean endPlace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
        ;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is blank");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = Strings.requireNonBlank(address, "address is blank");
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public boolean isStartPlace() {
        return startPlace;
    }

    public void setStartPlace(boolean startPlace) {
        this.startPlace = startPlace;
    }

    public boolean isEndPlace() {
        return endPlace;
    }

    public void setEndPlace(boolean endPlace) {
        this.endPlace = endPlace;
    }

}
