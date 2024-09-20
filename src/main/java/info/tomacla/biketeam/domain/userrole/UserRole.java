package info.tomacla.biketeam.domain.userrole;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_role")
public class UserRole {

    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private Team team;
    @Enumerated(EnumType.STRING)
    private Role role;

    public UserRole() {

    }

    public UserRole(Team team, User user, Role role) {
        this.id = getId(team, user);
        this.setTeam(team);
        this.setUser(user);
        this.setRole(role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(user);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public static String getId(Team team, User user) {
        return team.getId() + "-" + user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return id.equals(userRole.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
