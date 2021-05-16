package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "publication")
public class Publication {

    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(name= "published_status")
    private PublishedStatus publishedStatus;
    private String title;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt;
    @Column(name = "posted_at")
    private ZonedDateTime postedAt;
    @Column(length = 8000)
    private String content;
    private boolean imaged;

    protected Publication() {

    }

    public Publication(String title,
                       String content,
                       ZonedDateTime publishedAt,
                       boolean imaged) {
        this.id = UUID.randomUUID().toString();
        this.postedAt = ZonedDateTime.now();
        this.publishedStatus = PublishedStatus.UNPUBLISHED;
        setPublishedAt(publishedAt);
        setTitle(title);
        setContent(content);
        setImaged(imaged);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public PublishedStatus getPublishedStatus() {
        return publishedStatus;
    }

    public void setPublishedStatus(PublishedStatus publishedStatus) {
        this.publishedStatus = Objects.requireNonNull(publishedStatus);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Strings.requireNonBlank(title, "title is null");
    }

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt is null");
    }

    public ZonedDateTime getPostedAt() {
        return postedAt;
    }

    protected void setPostedAt(ZonedDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Strings.requireNonBlank(content, "content is null");
    }

    public boolean isImaged() {
        return imaged;
    }

    public void setImaged(boolean imaged) {
        this.imaged = imaged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Publication that = (Publication) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
