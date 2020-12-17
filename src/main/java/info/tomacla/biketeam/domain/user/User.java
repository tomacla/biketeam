package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.common.Strings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {

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

    protected User() {

    }

    public User(boolean admin,
                String firstName,
                String lastName,
                Long stravaId,
                String stravaUserName,
                String city,
                String profileImage) {
        this.id = UUID.randomUUID().toString();
        setAdmin(admin);
        setStravaId(stravaId);
        setStravaUserName(stravaUserName);
        setFirstName(firstName);
        setLastName(lastName);
        setCity(city);
        setProfileImage(profileImage);
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
}
