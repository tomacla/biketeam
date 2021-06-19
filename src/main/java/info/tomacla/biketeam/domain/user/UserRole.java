package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.team.Team;

import javax.persistence.*;

@Entity
@Table(name = "user_role")
public class UserRole {

    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    private Team team;
    @Enumerated(EnumType.STRING)
    private Role role;

    protected UserRole() {

    }

    public UserRole(Team team, User user, Role role) {
        this.id = team.getId() + "-" + user.getId();
        this.setTeam(team);
        this.setUser(user);
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

}
