package info.tomacla.biketeam.domain.global;

import info.tomacla.biketeam.common.Strings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "site_description")
public class SiteDescription {

    @Id
    private Long id = 1L;
    private String sitename;
    @Column(length = 2000)
    private String description;
    private String facebook;
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

    protected SiteDescription() {

    }

    public SiteDescription(String sitename,
                      String description) {
        setSitename(sitename);
        setDescription(description);
    }

    public SiteDescription(String sitename,
                      String description,
                      String facebook,
                      String twitter,
                      String email,
                      String phoneNumber,
                      String addressStreetLine,
                      String addressPostalCode,
                      String addressCity,
                      String other) {
        setSitename(sitename);
        setDescription(description);
        setFacebook(facebook);
        setTwitter(twitter);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddressStreetLine(addressStreetLine);
        setAddressPostalCode(addressPostalCode);
        setAddressCity(addressCity);
        setOther(other);
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = 1L;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = Strings.requireNonBlank(sitename, "sitename is null");
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


}
