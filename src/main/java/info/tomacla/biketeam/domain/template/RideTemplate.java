package info.tomacla.biketeam.domain.template;

import info.tomacla.biketeam.common.datatype.Lists;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.ride.RideType;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride_template")
public class RideTemplate {

    @Id
    @UuidGenerator
    private String id;
    @Column(name = "team_id")
    private String teamId;
    private String name;
    @Enumerated(EnumType.STRING)
    private RideType type = RideType.REGULAR;
    @Column(length = 8000)
    private String description;
    @OneToMany(mappedBy = "rideTemplate", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<RideGroupTemplate> groups = new HashSet<>();
    @Column
    private Integer increment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "start_place_id")
    private Place startPlace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "end_place_id")
    private Place endPlace;

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = Objects.requireNonNull(teamId, "teamId is null");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
    }

    public RideType getType() {
        return type;
    }

    public void setType(RideType type) {
        this.type = Objects.requireNonNullElse(type, RideType.REGULAR);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlank(description, "description is null");
    }

    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(Integer increment) {
        if (increment == null || increment < 0) {
            this.increment = null;
        } else {
            this.increment = increment;
        }
    }

    public Place getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(Place startPlace) {
        this.startPlace = startPlace;
    }

    public Place getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(Place endPlace) {
        this.endPlace = endPlace;
    }

    public Set<RideGroupTemplate> getGroups() {
        return groups;
    }

    public void setGroups(Set<RideGroupTemplate> groups) {
        Lists.requireNonEmpty(groups, "groups is null");
        groups.forEach(this::addGroup);
    }

    public List<RideGroupTemplate> getSortedGroups() {
        return groups.stream()
                .sorted(Comparator.comparing(RideGroupTemplate::getName))
                .collect(Collectors.toList());
    }

    public void addGroup(RideGroupTemplate group) {
        group.setRideTemplate(this);
        groups.add(group);
    }

    public void clearGroups() {
        this.groups.forEach(group -> group.setRideTemplate(null));
        this.groups.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RideTemplate that = (RideTemplate) o;
        return Objects.equals(id, that.id) && Objects.equals(teamId, that.teamId) && Objects.equals(name, that.name) && type == that.type && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamId, name, type, description);
    }
}
