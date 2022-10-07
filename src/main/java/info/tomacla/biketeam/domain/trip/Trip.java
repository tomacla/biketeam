package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.datatype.Lists;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.message.TripMessage;
import info.tomacla.biketeam.domain.place.Place;
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
    private String id = UUID.randomUUID().toString();
    @Column(name = "team_id")
    private String teamId;
    private String permalink;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "lower_speed")
    private double lowerSpeed;
    @Column(name = "upper_speed")
    private double upperSpeed;
    @Column(name = "meeting_time")
    private LocalTime meetingTime;
    @Enumerated(EnumType.STRING)
    private MapType type = MapType.ROAD;
    @Enumerated(EnumType.STRING)
    @Column(name = "published_status")
    private PublishedStatus publishedStatus = PublishedStatus.UNPUBLISHED;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    @Column(length = 8000)
    private String description;
    private boolean imaged;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "start_place_id")
    private Place startPlace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "end_place_id")
    private Place endPlace;
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<TripStage> stages = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "trip_participant",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new HashSet<>();
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<TripMessage> messages = new HashSet<>();

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
        this.teamId = Objects.requireNonNull(teamId, "team id is null");
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

    public MapType getType() {
        return type;
    }

    public void setType(MapType type) {
        this.type = Objects.requireNonNullElse(type, MapType.ROAD);
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

    public LocalTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalTime meetingTime) {
        this.meetingTime = Objects.requireNonNull(meetingTime);
    }

    public Set<TripStage> getStages() {
        return stages;
    }

    public void setStages(Set<TripStage> stages) {
        Lists.requireSizeOf(stages, 2, "stages is null");
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

        final Set<String> updatedStagesId = updatedStages.stream().map(TripStage::getId).filter(id -> !ObjectUtils.isEmpty(id)).collect(Collectors.toSet());
        final Set<String> existingStagesId = this.stages.stream().map(TripStage::getId).collect(Collectors.toSet());

        // remove deleted stages
        this.stages.forEach(stage -> {
            if (!updatedStagesId.contains(stage.getId())) {
                stage.setTrip(null); // needed for hibernate
            }
        });
        stages.removeIf(g -> !updatedStagesId.contains(g.getId()));

        // add new stages
        final List<TripStage> newStages = updatedStages.stream().filter(g -> !existingStagesId.contains(g.getId())).collect(Collectors.toList());
        newStages.forEach(this::addStage);

        // update existing stages
        final List<TripStage> existingStages = updatedStages.stream().filter(g -> existingStagesId.contains(g.getId())).collect(Collectors.toList());
        existingStages.forEach(g ->
                this.stages.stream().filter(gg -> g.getId().equals(gg.getId())).findFirst().ifPresent(target -> {
                    target.setName(g.getName());
                    target.setMap(g.getMap());
                    target.setDate(g.getDate());
                })
        );


    }

    public void addStage(TripStage stage) {
        stage.setTrip(this);
        stages.add(stage);
    }

    public boolean hasParticipant(String userId) {
        return this.getParticipants().stream().map(User::getId).anyMatch(uid -> uid.equals(userId));
    }

    public void addParticipant(User participant) {
        this.getParticipants().add(participant);
    }

    public void removeParticipant(User participant) {
        this.getParticipants().remove(participant);
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public List<User> getSortedParticipants() {
        return participants.stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    public void setParticipants(Set<User> participants) {
        this.participants = Objects.requireNonNullElse(participants, new HashSet<>());
    }

    public Set<TripMessage> getMessages() {
        return messages;
    }

    public void setMessages(Set<TripMessage> messages) {
        this.messages = Objects.requireNonNullElse(messages, new HashSet<>());
    }

    public List<TripMessage> getSortedMessages() {
        return messages.stream()
                .sorted(Comparator.comparing(TripMessage::getPublishedAt))
                .collect(Collectors.toList());
    }

    public void removeMessage(String messageId) {
        this.messages.removeIf(message -> message.getId().equals(messageId));
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
