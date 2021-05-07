package info.tomacla.biketeam.domain.feed;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "feed")
public class Feed {

    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    private FeedType type;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    private String title;
    private String content;
    private boolean imaged;

    protected Feed() {

    }

    public Feed(String id,
                FeedType type,
                ZonedDateTime publishedAt,
                String title,
                String content) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.publishedAt = Objects.requireNonNull(publishedAt);
        this.title = Objects.requireNonNull(title);
        this.content = Objects.requireNonNull(content);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDetailsUrl() {
        if(type.equals(FeedType.RIDE)) {
            return "/rides/" + getId();
        }
        return null;
    }

    public String getImageUrl() {
        if(imaged) {
            if(type.equals(FeedType.RIDE)) {
                return "/api/rides/" + getId() + "/image";
            }
            return "/api/publications/" + getId() + "/image";
        }
        return null;
    }

}
