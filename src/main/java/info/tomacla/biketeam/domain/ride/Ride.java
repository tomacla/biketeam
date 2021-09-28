package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.Lists;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride")
public class Ride {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    @Enumerated(EnumType.STRING)
    @Column(name = "published_status")
    private PublishedStatus publishedStatus;
    @Enumerated(EnumType.STRING)
    private RideType type;
    private LocalDate date;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    @Column(length = 8000)
    private String description;
    private boolean imaged;
    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<RideGroup> groups;

    @Transient
    private int nextGroupIndex = 0;

    protected Ride() {

    }

    public Ride(String teamId,
                RideType type,
                LocalDate date,
                ZonedDateTime publishedAt,
                String title,
                String description,
                boolean imaged,
                Set<RideGroup> groups) {

        this.id = UUID.randomUUID().toString();
        setTeamId(teamId);
        this.publishedStatus = PublishedStatus.UNPUBLISHED;
        setType(type);
        setDate(date);
        setPublishedAt(publishedAt);
        setTitle(title);
        setDescription(description);
        setImaged(imaged);
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

    public PublishedStatus getPublishedStatus() {
        return publishedStatus;
    }

    public void setPublishedStatus(PublishedStatus publishedStatus) {
        this.publishedStatus = Objects.requireNonNull(publishedStatus);
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

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt is null");
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

    public List<RideGroup> getSortedGroups() {
        return groups.stream()
                .sorted((r1, r2) -> {
                    if(!r1.getMeetingTime().equals(r2.getMeetingTime())) {
                        return r1.getMeetingTime().compareTo(r2.getMeetingTime());
                    }
                    return r1.getName().compareTo(r2.getName());
                })
                .collect(Collectors.toList());
    }

    public void addGroup(RideGroup group) {
        group.setRide(this, nextGroupIndex++);
        groups.add(group);
    }

    public void setGroups(Set<RideGroup> groups) {
        Lists.requireNonEmpty(groups, "groups is null");
        groups.forEach(this::addGroup);
    }

    public void clearGroups() {
        groups.clear();
    }

    public boolean hasParticipant(String id) {
        return this.getGroups().stream().map(g -> g.hasParticipant(id)).filter(p -> p).count() != 0L;
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
