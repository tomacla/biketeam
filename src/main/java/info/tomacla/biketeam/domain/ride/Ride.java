package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.Lists;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride")
public class Ride {

    @Id
    private String id;
    private RideType type;
    private LocalDate date;
    private String title;
    @Column(length = 8000)
    private String description;
    private boolean imaged;
    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RideGroup> groups;

    protected Ride() {

    }

    public Ride(RideType type,
                LocalDate date,
                String title,
                String description,
                boolean imaged,
                Set<RideGroup> groups) {

        this.id = UUID.randomUUID().toString();
        setType(type);
        setDate(date);
        setTitle(title);
        setDescription(description);
        setImaged(imaged);
        setGroups(groups);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public RideType getType() {
        return type;
    }

    public void setType(RideType type) {
        this.type = Objects.requireNonNull(type, "type is null");
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "date is null");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Strings.requireNonBlank(title, "title is null");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlank(description, "description is null");
    }

    public boolean isImaged() {
        return imaged;
    }

    public void setImaged(boolean imaged) {
        this.imaged = imaged;
    }

    public Set<RideGroup> getGroups() {
        return groups;
    }

    public void addGroup(RideGroup group) {
        group.setRide(this);
        groups.add(group);
    }

    public void setGroups(Set<RideGroup> groups) {
        Lists.requireNonEmpty(groups, "groups is null");
        this.groups = groups.stream().peek(g -> g.setRide(this)).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return id.equals(ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
