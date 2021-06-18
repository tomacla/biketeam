package info.tomacla.biketeam.domain.template;

import info.tomacla.biketeam.common.Lists;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.ride.RideType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ride_template")
public class RideTemplate {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    private String name;
    @Enumerated(EnumType.STRING)
    private RideType type;
    @Column(length = 8000)
    private String description;
    @OneToMany(mappedBy = "rideTemplate", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<RideGroupTemplate> groups;

    @Transient
    private int nextGroupIndex = 0;

    protected RideTemplate() {

    }

    public RideTemplate(String teamId,
                        String name,
                        RideType type,
                        String description,
                        Set<RideGroupTemplate> groups) {

        this.id = UUID.randomUUID().toString();
        setTeamId(teamId);
        setName(name);
        setType(type);
        setDescription(description);
        this.groups = Objects.requireNonNullElse(groups, new HashSet<>());
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
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

    public RideType getType() {
        return type;
    }

    public void setType(RideType type) {
        this.type = Objects.requireNonNull(type, "type is null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlank(description, "description is null");
    }

    public Set<RideGroupTemplate> getGroups() {
        return groups;
    }

    public void addGroup(RideGroupTemplate group) {
        group.setRideTemplate(this, nextGroupIndex++);
        groups.add(group);
    }

    public void setGroups(Set<RideGroupTemplate> groups) {
        Lists.requireNonEmpty(groups, "groups is null");
        groups.forEach(this::addGroup);
    }

    public void clearGroups() {
        this.groups.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideTemplate ride = (RideTemplate) o;
        return id.equals(ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
