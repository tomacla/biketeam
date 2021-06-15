package info.tomacla.biketeam.domain.feed;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "feed")
public class Feed {

    @Id
    private String id;
    @Column(name = "team_id")
    private String teamId;
    @Enumerated(EnumType.STRING)
    private FeedType type;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    private String content;
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

    // TODO should be url service
    public String getDetailsUrl(String teamId) {
        if (type.equals(FeedType.RIDE)) {
            return "/" + teamId + "/rides/" + getId();
        }
        return null;
    }

    // TODO should be url service
    public String getImageUrl(String teamId) {
        if (imaged) {
            if (type.equals(FeedType.RIDE)) {
                return "/api/" + teamId + "/rides/" + getId() + "/image";
            }
            return "/api/" + teamId + "/publications/" + getId() + "/image";
        }
        return null;
    }

}
