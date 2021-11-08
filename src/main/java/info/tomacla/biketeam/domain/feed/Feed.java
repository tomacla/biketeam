package info.tomacla.biketeam.domain.feed;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "feed")
public class Feed {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    @Column(name = "team_name")
    private String teamName;
    @Enumerated(EnumType.STRING)
    private FeedType type;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    private LocalDate date;
    private String content;
    @ElementCollection
    private List<String> badges;
    private boolean imaged;

    protected Feed() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public FeedType getType() {
        return type;
    }

    public void setType(FeedType type) {
        this.type = type;
    }

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isImaged() {
        return imaged;
    }

    public void setImaged(boolean imaged) {
        this.imaged = imaged;
    }

    public List<String> getBadges() {
        return badges.stream()
                .sorted(String::compareTo)
                .map(n -> n.substring(n.indexOf("-") + 1))
                .collect(Collectors.toList());
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }
}
