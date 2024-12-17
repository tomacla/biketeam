package info.tomacla.biketeam.domain.message;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @UuidGenerator
    private String id;
    @Column(name = "team_id")
    private String teamId;
    @Column(name = "target_id")
    private String targetId;

    @Column(name = "reply_to_id")
    private String replyToId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MessageTargetType type = MessageTargetType.RIDE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "published_at")
    private ZonedDateTime publishedAt = ZonedDateTime.now(ZoneOffset.UTC);
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public MessageTargetType getType() {
        return type;
    }

    public void setType(MessageTargetType type) {
        this.type = type;
    }

    public void setTarget(MessageHolder messageHolder) {
        this.targetId = messageHolder.getId();
        this.type = messageHolder.getMessageType();
        this.teamId = messageHolder.getTeamId();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(user);
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
        this.content = Strings.requireNonBlank(content, "content is null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
