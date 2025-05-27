package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "map")
public class Map {

    @Id
    @UuidGenerator
    private String id;
    @Column(name = "team_id")
    private String teamId;
    private String permalink;
    private String name;
    private double length;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MapType type = MapType.ROAD;
    @Column(name = "positive_elevation")
    private double positiveElevation;
    @Column(name = "negative_elevation")
    private double negativeElevation;
    @Column(name = "posted_at")
    private LocalDate postedAt = LocalDate.now(ZoneOffset.UTC);
    @ElementCollection
    private List<String> tags = new ArrayList<>();
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
    private WindDirection windDirection = WindDirection.NORTH;
    private boolean crossing;

    private boolean deletion;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = Objects.requireNonNull(teamId, "teamId is null");
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = Strings.requireNonBlankOrNull(permalink);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is blank");
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
        this.type = Objects.requireNonNullElse(type, MapType.ROAD);
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
        this.postedAt = Objects.requireNonNull(postedAt, "postedAt is null");
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = Objects.requireNonNullElse(new ArrayList<>(tags), new ArrayList<>());
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = Objects.requireNonNull(startPoint, "startPoint is null");
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = Objects.requireNonNull(endPoint, "endPoint is null");
    }

    public WindDirection getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(WindDirection windDirection) {
        this.windDirection = Objects.requireNonNull(windDirection, "windDirection is null");
    }

    public boolean isCrossing() {
        return crossing;
    }

    public void setCrossing(boolean crossing) {
        this.crossing = crossing;
    }

    public boolean isDeletion() {
        return deletion;
    }

    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount != null ? ratingCount : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Map map = (Map) o;
        return Double.compare(length, map.length) == 0 && Objects.equals(id, map.id) && Objects.equals(teamId, map.teamId) && Objects.equals(permalink, map.permalink) && Objects.equals(name, map.name) && type == map.type && Objects.equals(postedAt, map.postedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamId, permalink, name, length, type, postedAt);
    }
}
