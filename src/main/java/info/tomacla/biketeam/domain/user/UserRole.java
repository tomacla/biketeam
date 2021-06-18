package info.tomacla.biketeam.domain.user;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleKey id;
    @Enumerated(EnumType.STRING)
    private Role role;

    protected UserRole() {

    }

    public UserRole(String userId, String teamId, Role role) {
        UserRoleKey key = new UserRoleKey();
        key.setUserId(userId);
        key.setTeamId(teamId);
        this.id = key;
        this.role = role;
    }

    public UserRoleKey getId() {
        return id;
    }

    public void setId(UserRoleKey id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static UserRole admin(String userId, String teamId) {
        return new UserRole(userId, teamId, Role.ADMIN);
    }

    public static UserRole member(String userId, String teamId) {
        return new UserRole(userId, teamId, Role.MEMBER);
    }

    public boolean isAdmin(String teamId) {
        return this.role.equals(Role.ADMIN) && this.getId().getTeamId().equals(teamId);
    }

    public boolean isMember(String teamId) {
        return this.role.equals(Role.MEMBER) && this.getId().getTeamId().equals(teamId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return id.equals(userRole.id) && role == userRole.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role);
    }
}
