package info.tomacla.biketeam.domain.global;

import info.tomacla.biketeam.common.Lists;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.ride.RideType;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride_template")
public class RideTemplate {

    @Id
    private String id;
    private String name;
    private RideType type;
    @Column(length = 8000)
    private String description;
    @OneToMany(mappedBy = "rideTemplate", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RideGroupTemplate> groups;

    protected RideTemplate() {

    }

    public RideTemplate(String name,
                        RideType type,
                        String description,
                        Set<RideGroupTemplate> groups) {

        this.id = UUID.randomUUID().toString();
        setName(name);
        setType(type);
        setDescription(description);
        setGroups(groups);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
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
        group.setRideTemplate(this);
        groups.add(group);
    }

    public void setGroups(Set<RideGroupTemplate> groups) {
        Lists.requireNonEmpty(groups, "groups is null");
        this.groups = groups.stream().peek(g -> g.setRideTemplate(this)).collect(Collectors.toSet());
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
