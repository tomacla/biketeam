package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.datatype.Strings;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "team_description")
public class TeamDescription {

    @Id
    @Column(name = "team_id")
    private String teamId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "team_id")
    private Team team;
    @Column(length = 2000)
    private String description;
    private String facebook;
    private String instagram;
    private String twitter;
    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "address_street_line")
    private String addressStreetLine;
    @Column(name = "address_postal_code", length = 10)
    private String addressPostalCode;
    @Column(name = "address_postal_city")
    private String addressCity;
    @Column(length = 2000)
    private String other;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = Objects.requireNonNull(teamId, "teamId is null");
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = Objects.requireNonNull(team);
        this.teamId = team.getId();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlank(description, "description is null");
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = Strings.requireNonBlankOrNull(facebook);
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = Strings.requireNonBlankOrNull(instagram);
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = Strings.requireNonBlankOrNull(twitter);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrNull(email);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = Strings.requireNonBlankOrNull(phoneNumber);
    }

    public String getAddressStreetLine() {
        return addressStreetLine;
    }

    public void setAddressStreetLine(String addressStreetLine) {
        this.addressStreetLine = Strings.requireNonBlankOrNull(addressStreetLine);
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = Strings.requireNonBlankOrNull(addressPostalCode);
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = Strings.requireNonBlankOrNull(addressCity);
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = Strings.requireNonBlankOrNull(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDescription that = (TeamDescription) o;
        return teamId.equals(that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}
