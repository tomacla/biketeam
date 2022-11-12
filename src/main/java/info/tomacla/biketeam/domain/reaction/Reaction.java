package info.tomacla.biketeam.domain.reaction;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reaction")
public class Reaction {

    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "team_id")
    private String teamId;
    @Column(name = "target_id")
    private String targetId;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ReactionTargetType type = ReactionTargetType.RIDE;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
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

    public ReactionTargetType getType() {
        return type;
    }

    public void setType(ReactionTargetType type) {
        this.type = type;
    }

    public void setTarget(ReactionHolder reactionHolder) {
        this.targetId = reactionHolder.getId();
        this.type = reactionHolder.getReactionType();
        this.teamId = reactionHolder.getTeamId();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(user);
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
        Reaction that = (Reaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}