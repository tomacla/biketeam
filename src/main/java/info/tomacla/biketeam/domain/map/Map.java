package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "map")
public class Map {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    private String name;
    private double length;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MapType type;
    @Column(name = "positive_elevation")
    private double positiveElevation;
    @Column(name = "negative_elevation")
    private double negativeElevation;
    @Column(name = "posted_at")
    private LocalDate postedAt;
    @ElementCollection
    private List<String> tags;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "start_point_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "start_point_lng"))
    })
    @Embedded
    private Point startPoint;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "end_point_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "end_point_lng"))
    })
    @Embedded
    private Point endPoint;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "wind_vector_x")),
            @AttributeOverride(name = "y", column = @Column(name = "wind_vector_y"))
    })
    @Column(name = "wind_direction")
    @Enumerated(EnumType.STRING)
    private WindDirection windDirection;
    private boolean crossing;
    private boolean visible;

    protected Map() {

    }

    public Map(String id,
               String teamId,
               String name,
               double length,
               MapType type,
               LocalDate postedAt,
               double positiveElevation,
               double negativeElevation,
               List<String> tags,
               Point startPoint,
               Point endPoint,
               WindDirection windDirection,
               boolean crossing,
               boolean visible) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        setTeamId(teamId);
        setPostedAt(postedAt);
        setType(type);
        setName(name);
        setLength(length);
        setPositiveElevation(positiveElevation);
        setNegativeElevation(negativeElevation);
        setTags(new ArrayList<>(tags));
        setStartPoint(startPoint);
        setEndPoint(endPoint);
        setWindDirection(windDirection);
        setCrossing(crossing);
        setVisible(visible);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = Objects.requireNonNull(teamId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public MapType getType() {
        return type;
    }

    public void setType(MapType type) {
        this.type = Objects.requireNonNull(type);
    }

    public double getPositiveElevation() {
        return positiveElevation;
    }

    public void setPositiveElevation(double positiveElevation) {
        this.positiveElevation = positiveElevation;
    }

    public double getNegativeElevation() {
        return negativeElevation;
    }

    public void setNegativeElevation(double negativeElevation) {
        this.negativeElevation = negativeElevation;
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDate postedAt) {
        this.postedAt = postedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    public WindDirection getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(WindDirection windDirection) {
        this.windDirection = Objects.requireNonNull(windDirection);
    }

    public boolean isCrossing() {
        return crossing;
    }

    public void setCrossing(boolean crossing) {
        this.crossing = crossing;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getDescription() {

        StringBuilder sb = new StringBuilder();
        sb.append("Distance ").append(length).append("km").append(" - ");
        sb.append(positiveElevation).append("m D+").append(" - ");
        sb.append(type.getLabel());
        if (!tags.isEmpty()) {
            sb.append(" - ");
            tags.forEach(t -> sb.append(t).append(", "));
        }

        return sb.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Map that = (Map) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
