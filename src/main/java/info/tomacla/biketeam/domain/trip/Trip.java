package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.Lists;
import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.user.User;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "trip")
public class Trip {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "lower_speed")
    private double lowerSpeed;
    @Column(name = "upper_speed")
    private double upperSpeed;
    @Column(name = "meeting_location")
    private String meetingLocation;
    @Column(name = "meeting_time")
    private LocalTime meetingTime;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "meeting_point_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "meeting_point_lng"))
    })
    @Embedded
    private Point meetingPoint;
    @Enumerated(EnumType.STRING)
    private MapType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "published_status")
    private PublishedStatus publishedStatus;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    @Column(length = 8000)
    private String description;
    private boolean imaged;
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<TripStage> stages;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "trip_participant",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants;

    public Trip() {
    }

    public Trip(String teamId,
                MapType type,
                LocalDate startDate,
                LocalDate endDate,
                double lowerSpeed,
                double upperSpeed,
                ZonedDateTime publishedAt,
                String title,
                String description,
                boolean imaged,
                String meetingLocation,
                LocalTime meetingTime,
                Point meetingPoint) {

        this.id = UUID.randomUUID().toString();
        setTeamId(teamId);
        this.publishedStatus = PublishedStatus.UNPUBLISHED;
        setType(type);
        setStartDate(startDate);
        setEndDate(endDate);
        setPublishedAt(publishedAt);
        setTitle(title);
        setDescription(description);
        setImaged(imaged);
        setMeetingLocation(meetingLocation);
        setMeetingTime(meetingTime);
        setMeetingPoint(meetingPoint);
        setLowerSpeed(lowerSpeed);
        setUpperSpeed(upperSpeed);

        this.participants = new HashSet<>();
        this.stages = new HashSet<>();
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

    public MapType getType() {
        return type;
    }

    public void setType(MapType type) {
        this.type = Objects.requireNonNull(type, "type is null");
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = Objects.requireNonNull(startDate, "startDate is null");
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = Objects.requireNonNull(endDate, "endDate is null");
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

    public double getLowerSpeed() {
        return lowerSpeed;
    }

    public void setLowerSpeed(double lowerSpeed) {
        this.lowerSpeed = lowerSpeed;
    }

    public double getUpperSpeed() {
        return upperSpeed;
    }

    public void setUpperSpeed(double upperSpeed) {
        this.upperSpeed = upperSpeed;
    }

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = Strings.requireNonBlankOrNull(meetingLocation);
    }

    public LocalTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalTime meetingTime) {
        this.meetingTime = Objects.requireNonNull(meetingTime);
    }

    public Point getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Point meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public Set<TripStage> getStages() {
        return stages;
    }

    public void setStages(Set<TripStage> stages) {
        Lists.requireNonEmpty(stages, "stages is null");
        stages.forEach(this::addStage);
    }

    public List<TripStage> getSortedStages() {
        return stages.stream()
                .sorted((r1, r2) -> {
                    if (!r1.getDate().equals(r2.getDate())) {
                        return r1.getDate().compareTo(r2.getDate());
                    }
                    return r1.getName().compareTo(r2.getName());
                })
                .collect(Collectors.toList());
    }

    public void addOrReplaceStages(List<TripStage> updatedStages) {

        // remove deleted stages
        final Set<String> updatedStagesId = updatedStages.stream().map(TripStage::getId).filter(id -> !ObjectUtils.isEmpty(id)).collect(Collectors.toSet());
        this.stages.forEach(stage -> {
            if (!updatedStagesId.contains(stage.getId())) {
                stage.removeTrip(); // needed for hibernate
            }
        });
        stages.removeIf(g -> !updatedStagesId.contains(g.getId()));

        // add new stages
        final List<TripStage> newStages = updatedStages.stream().filter(g -> g.getId() == null).collect(Collectors.toList());
        newStages.forEach(this::addStage);

        // update existing stages
        final List<TripStage> existingStages = updatedStages.stream().filter(g -> g.getId() != null).collect(Collectors.toList());
        existingStages.forEach(g ->
                this.stages.stream().filter(gg -> g.getId().equals(gg.getId())).findFirst().ifPresent(target -> {
                    target.setName(g.getName());
                    target.setMapId(g.getMapId());
                    target.setDate(g.getDate());
                })
        );


    }

    public void addStage(TripStage stage) {
        stage.setTrip(this, getNextStageIndex());
        stages.add(stage);
    }

    public void clearStages() {
        stages.clear();
    }

    private int getNextStageIndex() {
        final Optional<Integer> nextIndex = this.stages.stream().map(TripStage::getStageIndex).max(Comparator.naturalOrder());
        if (nextIndex.isEmpty()) {
            return 0;
        }
        return nextIndex.get() + 1;
    }

    public boolean hasParticipant(String userId) {
        return this.getParticipants().stream().map(User::getId).anyMatch(uid -> uid.equals(userId));
    }

    public void addParticipant(User participant) {
        this.getParticipants().add(participant);
    }

    public void removeParticipant(User participant) {
        this.getParticipants().removeIf(u -> u.equals(participant));
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = Objects.requireNonNullElse(participants, new HashSet<>());
    }

    public List<User> getSortedParticipants() {
        return participants.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return id.equals(trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
