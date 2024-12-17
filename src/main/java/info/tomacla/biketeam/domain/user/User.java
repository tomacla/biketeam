package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.userrole.UserRole;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class User {

    @Id
    @UuidGenerator
    private String id;
    private boolean admin = false;

    private boolean deletion;

    @Column(name = "strava_id", unique = true)
    private Long stravaId;
    @Column(name = "facebook_id", unique = true)
    private String facebookId;
    @Column(name = "google_id", unique = true)
    private String googleId;
    @Column(name = "strava_user_name", unique = true)
    private String stravaUserName;
    @Column(name = "garmin_token")
    private String garminToken;
    @Column(name = "garmin_token_secret")
    private String garminTokenSecret;
    @Column(name = "first_name")
    private String firstName = "Inconnu";
    @Column(name = "last_name")
    private String lastName = "Inconnu";
    @Column(name = "city")
    private String city;
    @Column(name = "email")
    private String email;
    @Column(name = "email_publish_trips")
    private boolean emailPublishTrips;
    @Column(name = "email_publish_rides")
    private boolean emailPublishRides;
    @Column(name = "email_publish_publications")
    private boolean emailPublishPublications;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "team_id", unique = true)
    private String teamId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "map_favorite",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "map_id"))
    private Set<Map> mapFavorites = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
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

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
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
        this.city = Strings.requireNonBlankOrNull(city);
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = Objects.requireNonNullElse(roles, new HashSet<>());
    }

    public void removeUserRole(UserRole userRole) {
        this.roles.remove(userRole);
    }

    public String getIdentity() {
        return getFirstName() + " " + getLastName();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (Strings.isEmail(email)) {
            this.email = email.toLowerCase();
        } else {
            this.email = null;
        }
    }

    public boolean isEmailPublishTrips() {
        return emailPublishTrips;
    }

    public void setEmailPublishTrips(boolean emailPublishTrips) {
        this.emailPublishTrips = emailPublishTrips;
    }

    public boolean isEmailPublishRides() {
        return emailPublishRides;
    }

    public void setEmailPublishRides(boolean emailPublishRides) {
        this.emailPublishRides = emailPublishRides;
    }

    public boolean isEmailPublishPublications() {
        return emailPublishPublications;
    }

    public void setEmailPublishPublications(boolean emailPublishPublications) {
        this.emailPublishPublications = emailPublishPublications;
    }

    public Set<Map> getMapFavorites() {
        return mapFavorites;
    }

    public void setMapFavorites(Set<Map> mapFavorites) {
        this.mapFavorites = mapFavorites;
    }

    public String getGarminToken() {
        return garminToken;
    }

    public String getGarminTokenSecret() {
        return garminTokenSecret;
    }

    public void setGarminToken(String garminToken) {
        this.garminToken = garminToken;
    }

    public void setGarminTokenSecret(String garminTokenSecret) {
        this.garminTokenSecret = garminTokenSecret;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public boolean isDeletion() {
        return deletion;
    }

    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
