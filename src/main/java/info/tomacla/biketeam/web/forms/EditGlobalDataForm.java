package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.domain.global.GlobalData;

public class EditGlobalDataForm {

    private String sitename;
    private String description;
    private String facebook;
    private String twitter;
    private String email;
    private String phoneNumber;
    private String addressStreetLine;
    private String addressPostalCode;
    private String addressCity;
    private String other;
    private String mapBoxAPIKey;

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFacebook() {
        return facebook == null ? "" : facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter == null ? "" : twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber == null ? "" : phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddressStreetLine() {
        return addressStreetLine == null ? "" : addressStreetLine;
    }

    public void setAddressStreetLine(String addressStreetLine) {
        this.addressStreetLine = addressStreetLine;
    }

    public String getAddressPostalCode() {
        return addressPostalCode == null ? "" : addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressCity() {
        return addressCity == null ? "" : addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getOther() {
        return other == null ? "" : other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getMapBoxAPIKey() {
        return mapBoxAPIKey == null ? "" : mapBoxAPIKey;
    }

    public void setMapBoxAPIKey(String mapBoxAPIKey) {
        this.mapBoxAPIKey = mapBoxAPIKey;
    }

    public static EditGlobalDataForm build(GlobalData obj) {
        EditGlobalDataForm form = new EditGlobalDataForm();
        form.sitename = obj.getSitename();
        form.description = obj.getDescription();
        form.facebook = obj.getFacebook();
        form.twitter = obj.getTwitter();
        form.email = obj.getEmail();
        form.phoneNumber = obj.getPhoneNumber();
        form.addressStreetLine = obj.getAddressStreetLine();
        form.addressPostalCode = obj.getAddressPostalCode();
        form.addressCity = obj.getAddressCity();
        form.other = obj.getOther();
        form.mapBoxAPIKey = obj.getMapBoxAPIKey();
        return form;
    }

}
