package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.datatype.Strings;

import java.util.Objects;

public class EditTeamDescriptionForm {

    private String facebook = "";
    private String instagram = "";
    private String twitter = "";
    private String email = "";
    private String phoneNumber = "";
    private String addressStreetLine = "";
    private String addressPostalCode = "";
    private String addressCity = "";
    private String other = "";

    public static EditTeamDescriptionFormBuilder builder() {
        return new EditTeamDescriptionFormBuilder();
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = Objects.requireNonNullElse(facebook, "");
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = Objects.requireNonNullElse(instagram, "");
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = Objects.requireNonNullElse(twitter, "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNullElse(email, "");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = Objects.requireNonNullElse(phoneNumber, "");
    }

    public String getAddressStreetLine() {
        return addressStreetLine;
    }

    public void setAddressStreetLine(String addressStreetLine) {
        this.addressStreetLine = Objects.requireNonNullElse(addressStreetLine, "");
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = Objects.requireNonNullElse(addressPostalCode, "");
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = Objects.requireNonNullElse(addressCity, "");
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = Objects.requireNonNullElse(other, "");
    }

    public EditTeamDescriptionFormParser parser() {
        return new EditTeamDescriptionFormParser(this);
    }

    public static class EditTeamDescriptionFormParser {

        private final EditTeamDescriptionForm form;

        protected EditTeamDescriptionFormParser(EditTeamDescriptionForm form) {
            this.form = form;
        }

        public String getFacebook() {
            return Strings.requireNonBlankOrNull(form.getFacebook());
        }

        public String getInstagram() {
            return Strings.requireNonBlankOrNull(form.getInstagram());
        }

        public String getTwitter() {
            return Strings.requireNonBlankOrNull(form.getTwitter());
        }

        public String getEmail() {
            return Strings.requireNonBlankOrNull(form.getEmail());
        }

        public String getPhoneNumber() {
            return Strings.requireNonBlankOrNull(form.getPhoneNumber());
        }

        public String getAddressStreetLine() {
            return Strings.requireNonBlankOrNull(form.getAddressStreetLine());
        }

        public String getAddressPostalCode() {
            return Strings.requireNonBlankOrNull(form.getAddressPostalCode());
        }

        public String getAddressCity() {
            return Strings.requireNonBlankOrNull(form.getAddressCity());
        }

        public String getOther() {
            return Strings.requireNonBlankOrNull(form.getOther());
        }


    }

    public static class EditTeamDescriptionFormBuilder {

        private final EditTeamDescriptionForm form;

        protected EditTeamDescriptionFormBuilder() {
            this.form = new EditTeamDescriptionForm();
        }

        public EditTeamDescriptionFormBuilder withFacebook(String facebook) {
            form.setFacebook(facebook);
            return this;
        }

        public EditTeamDescriptionFormBuilder withInstagram(String instagram) {
            form.setInstagram(instagram);
            return this;
        }

        public EditTeamDescriptionFormBuilder withTwitter(String twitter) {
            form.setTwitter(twitter);
            return this;
        }

        public EditTeamDescriptionFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public EditTeamDescriptionFormBuilder withPhoneNumber(String phoneNumber) {
            form.setPhoneNumber(phoneNumber);
            return this;
        }

        public EditTeamDescriptionFormBuilder withAddressStreetLine(String addressStreetLine) {
            form.setAddressStreetLine(addressStreetLine);
            return this;
        }

        public EditTeamDescriptionFormBuilder withAddressPostalCode(String addressPostalCode) {
            form.setAddressPostalCode(addressPostalCode);
            return this;
        }

        public EditTeamDescriptionFormBuilder withAddressCity(String addressCity) {
            form.setAddressCity(addressCity);
            return this;
        }

        public EditTeamDescriptionFormBuilder withOther(String other) {
            form.setOther(other);
            return this;
        }

        public EditTeamDescriptionForm get() {
            return form;
        }

    }

}
