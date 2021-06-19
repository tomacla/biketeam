package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class User implements Serializable {

    @Id
    private String id;
    private boolean admin;
    @Column(name = "strava_id", unique = true)
    private Long stravaId;
    @Column(name = "strava_user_name", unique = true)
    private String stravaUserName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "city")
    private String city;
    @Column(name = "profile_image", length = 500)
    private String profileImage;
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<RideGroup> rideGroups;
    @Column(name = "email")
    private String email;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserRole> roles;

    protected User() {

    }

    public User(boolean admin,
                String firstName,
                String lastName,
                Long stravaId,
                String stravaUserName,
                String city,
                String profileImage,
                Set<RideGroup> rideGroups,
                Set<UserRole> roles) {
        this.id = UUID.randomUUID().toString();
        setAdmin(admin);
        setStravaId(stravaId);
        setStravaUserName(stravaUserName);
        setFirstName(firstName);
        setLastName(lastName);
        setCity(city);
        setProfileImage(profileImage);
        setRideGroups(rideGroups);
        this.roles = Objects.requireNonNullElse(roles, new HashSet<>());
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Long getStravaId() {
        return stravaId;
    }

    public void setStravaId(Long stravaId) {
        this.stravaId = stravaId;
    }

    public String getStravaUserName() {
        return stravaUserName;
    }

    public void setStravaUserName(String stravaUserName) {
        this.stravaUserName = stravaUserName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Strings.requireNonBlank(firstName, "firstname is null");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Strings.requireNonBlank(lastName, "lastname is null");
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Set<RideGroup> getRideGroups() {
        return rideGroups;
    }

    public void setRideGroups(Set<RideGroup> rideGroups) {
        this.rideGroups = Objects.requireNonNullElse(rideGroups, new HashSet<>());
    }

    public String getIdentity() {
        return getFirstName() + " " + getLastName();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrNull(email);
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void clearRoles() {
        this.roles.clear();
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void addRole(Team team, Role role) {
        this.roles.add(new UserRole(team, this, role));
    }

    public void removeRole(String teamId) {
        this.roles.removeIf(r -> r.getTeam().getId().equals(teamId));
    }

    public boolean isAdmin(String teamId) {
        for (UserRole role : this.roles) {
            if (role.getTeam().getId().equals(teamId) && role.getRole().equals(Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMember(String teamId) {
        for (UserRole role : this.roles) {
            if (role.getTeam().getId().equals(teamId) && role.getRole().equals(Role.MEMBER)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdminOrMember(String teamId) {
        return isAdmin(teamId) || isMember(teamId);
    }


}
