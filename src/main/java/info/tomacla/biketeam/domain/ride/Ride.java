package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.datatype.Lists;
import info.tomacla.biketeam.common.datatype.Strings;
import org.springframework.util.ObjectUtils;

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
    private String permalink;
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
    private Set<RideGroup> groups;

    public Ride() {
    }

    public Ride(String teamId,
                String permalink,
                RideType type,
                LocalDate date,
                ZonedDateTime publishedAt,
                String title,
                String description,
                boolean imaged,
                Set<RideGroup> groups) {

        this.id = UUID.randomUUID().toString();

        setTeamId(teamId);
        setPermalink(permalink);
        setPublishedStatus(PublishedStatus.UNPUBLISHED);
        setType(type);
        setDate(date);
        setPublishedAt(publishedAt);
        setTitle(title);
        setPermalink(Strings.normalizePermalink(title));
        setDescription(description);
        setImaged(imaged);

        this.groups = new HashSet<>();

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

    public void setGroups(Set<RideGroup> groups) {
        Lists.requireSizeOf(groups, 1, "groups is null or empty");
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

        // remove deleted groups
        final Set<String> updatedGroupsId = updatedGroups.stream().map(RideGroup::getId).filter(id -> !ObjectUtils.isEmpty(id)).collect(Collectors.toSet());
        this.groups.forEach(group -> {
            if (!updatedGroupsId.contains(group.getId())) {
                group.removeRide(); // needed for hibernate
            }
        });
        groups.removeIf(g -> !updatedGroupsId.contains(g.getId()));

        // add new groups
        final List<RideGroup> newGroups = updatedGroups.stream().filter(g -> g.getId() == null).collect(Collectors.toList());
        newGroups.forEach(this::addGroup);

        // update existing groups
        final List<RideGroup> existingGroups = updatedGroups.stream().filter(g -> g.getId() != null).collect(Collectors.toList());
        existingGroups.forEach(g ->
                this.groups.stream().filter(gg -> g.getId().equals(gg.getId())).findFirst().ifPresent(target -> {
                    target.setMeetingLocation(g.getMeetingLocation());
                    target.setMeetingTime(g.getMeetingTime());
                    target.setName(g.getName());
                    target.setUpperSpeed(g.getUpperSpeed());
                    target.setLowerSpeed(g.getLowerSpeed());
                    target.setMap(g.getMap());
                })
        );

    }

    public void addGroup(RideGroup group) {
        group.setRide(this, getNextGroupIndex());
        groups.add(group);
    }

    public void clearGroups() {
        groups.clear();
    }

    private int getNextGroupIndex() {
        final Optional<Integer> nextIndex = this.groups.stream().map(RideGroup::getGroupIndex).max(Comparator.naturalOrder());
        if (nextIndex.isEmpty()) {
            return 0;
        }
        return nextIndex.get() + 1;
    }

    public boolean hasParticipant(String userId) {
        return this.getGroups().stream().map(g -> g.hasParticipant(userId)).filter(p -> p).count() != 0L;
    }

    public boolean isParticipantInAnotherGroup(String groupId, String userId) {
        for (RideGroup group : this.getGroups()) {
            if (!group.getId().equals(groupId) && group.hasParticipant(userId)) {
                return true;
            }
        }
        return false;
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
