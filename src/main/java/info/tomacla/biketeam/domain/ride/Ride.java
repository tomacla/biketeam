package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.datatype.Lists;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedType;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageTargetType;
import info.tomacla.biketeam.domain.place.Place;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "ride")
public class Ride implements MessageHolder, FeedEntity {
    @Id
    @UuidGenerator
    private String id;
    @Column(name = "team_id")
    private String teamId;
    private String permalink;
    @Enumerated(EnumType.STRING)
    @Column(name = "published_status")
    private PublishedStatus publishedStatus = PublishedStatus.UNPUBLISHED;
    @Enumerated(EnumType.STRING)
    private RideType type = RideType.REGULAR;
    private LocalDate date;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt = ZonedDateTime.now(ZoneOffset.UTC);
    private String title;
    @Column(length = 8000)
    private String description;

    private boolean imaged;

    @Column(name = "listed_in_feed")
    private boolean listedInFeed;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "start_place_id")
    private Place startPlace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "end_place_id")
    private Place endPlace;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<RideGroup> groups = new HashSet<>();

    private boolean deletion;

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

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = Strings.requireNonBlankOrNull(permalink);
    }

    public PublishedStatus getPublishedStatus() {
        return publishedStatus;
    }

    public void setPublishedStatus(PublishedStatus publishedStatus) {
        this.publishedStatus = Objects.requireNonNull(publishedStatus, "publishedStatus is null");
    }

    public RideType getType() {
        return type;
    }

    public void setType(RideType type) {
        this.type = Objects.requireNonNullElse(type, RideType.REGULAR);
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
        this.title = Strings.requireNonBlank(title, "title is blank");
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

    public boolean isListedInFeed() {
        return listedInFeed;
    }

    public void setListedInFeed(boolean listedInFeed) {
        this.listedInFeed = listedInFeed;
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

    public Set<RideGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<RideGroup> groups) {
        Lists.requireSizeOf(groups, 1, "groups must have at least 1 element");
        groups.forEach(this::addGroup);
    }

    public List<RideGroup> getSortedGroups() {
        return groups.stream()
                .sorted((r1, r2) -> {
                    if (!r1.getMeetingTime().equals(r2.getMeetingTime())) {
                        return r1.getMeetingTime().compareTo(r2.getMeetingTime());
                    }
                    return r1.getName().compareTo(r2.getName());
                })
                .collect(Collectors.toList());
    }

    public void addOrReplaceGroups(List<RideGroup> updatedGroups) {

        final Set<String> updatedGroupsId = updatedGroups.stream().map(RideGroup::getId).filter(id -> !ObjectUtils.isEmpty(id)).collect(Collectors.toSet());
        final Set<String> existingGroupsId = this.groups.stream().map(RideGroup::getId).collect(Collectors.toSet());

        // remove deleted groups
        this.groups.forEach(group -> {
            if (!updatedGroupsId.contains(group.getId())) {
                group.setRide(null); // needed for hibernate
            }
        });
        groups.removeIf(g -> !updatedGroupsId.contains(g.getId()));

        // add new groups
        final List<RideGroup> newGroups = updatedGroups.stream().filter(g -> !existingGroupsId.contains(g.getId())).collect(Collectors.toList());
        newGroups.forEach(this::addGroup);

        // update existing groups
        final List<RideGroup> existingGroups = updatedGroups.stream().filter(g -> existingGroupsId.contains(g.getId())).collect(Collectors.toList());
        existingGroups.forEach(g ->
                this.groups.stream().filter(gg -> g.getId().equals(gg.getId())).findFirst().ifPresent(target -> {
                    target.setMeetingTime(g.getMeetingTime());
                    target.setName(g.getName());
                    target.setAverageSpeed(g.getAverageSpeed());
                    target.setMap(g.getMap());
                })
        );

    }

    public void addGroup(RideGroup group) {
        group.setRide(this);
        groups.add(group);
    }

    public boolean hasParticipant(String userId) {
        return this.getGroups().stream().anyMatch(g -> g.hasParticipant(userId));
    }

    public boolean isParticipantInAnotherGroup(String groupId, String userId) {
        for (RideGroup group : this.getGroups()) {
            if (!group.getId().equals(groupId) && group.hasParticipant(userId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeletion() {
        return deletion;
    }

    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }

    @Override
    public MessageTargetType getMessageType() {
        return MessageTargetType.RIDE;
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.RIDE;
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
