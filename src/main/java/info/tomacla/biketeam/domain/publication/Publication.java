package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedType;
import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionHolder;
import info.tomacla.biketeam.domain.reaction.ReactionTargetType;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "publication")
public class Publication implements ReactionHolder, FeedEntity {

    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "team_id")
    private String teamId;
    @Enumerated(EnumType.STRING)
    @Column(name = "published_status")
    private PublishedStatus publishedStatus = PublishedStatus.UNPUBLISHED;
    private String title;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt = ZonedDateTime.now(ZoneOffset.UTC);
    @Column(length = 8000)
    private String content;
    private boolean imaged;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    @Where(clause = "type = 'PUBLICATION'")
    private Set<Reaction> reactions = new HashSet<>();

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

    public PublishedStatus getPublishedStatus() {
        return publishedStatus;
    }

    public void setPublishedStatus(PublishedStatus publishedStatus) {
        this.publishedStatus = Objects.requireNonNull(publishedStatus, "publishedStatus is null");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Strings.requireNonBlank(title, "title is blank");
    }

    public ZonedDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt is null");
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Strings.requireNonBlank(content, "content is blank");
    }

    public boolean isImaged() {
        return imaged;
    }

    public void setImaged(boolean imaged) {
        this.imaged = imaged;
    }

    public Set<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(Set<Reaction> reactions) {
        this.reactions = reactions;
    }

    @Override
    public ReactionTargetType getReactionType() {
        return ReactionTargetType.PUBLICATION;
    }

    @Override
    public LocalDate getDate() {
        return getPublishedAt().toLocalDate();
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.PUBLICATION;
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
