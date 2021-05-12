package info.tomacla.biketeam.web.admin.configuration;

import java.util.Objects;

public class EditSiteDescriptionForm {

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

    public EditSiteDescriptionForm() {
        setSitename("");
        setDescription("");
        setFacebook("");
        setTwitter("");
        setEmail("");
        setPhoneNumber("");
        setAddressCity("");
        setAddressPostalCode("");
        setAddressStreetLine("");
        setOther("");
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = Objects.requireNonNullElse(sitename, "");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = Objects.requireNonNullElse(facebook, "");
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

    public EditSiteDescriptionFormParser parser() {
        return new EditSiteDescriptionFormParser(this);
    }

    public static EditSiteDescriptionFormBuilder builder() {
        return new EditSiteDescriptionFormBuilder();
    }

    public static class EditSiteDescriptionFormParser {

        private final EditSiteDescriptionForm form;

        protected EditSiteDescriptionFormParser(EditSiteDescriptionForm form) {
            this.form = form;
        }

        public String getSitename() {
            return form.getSitename();
        }

        public String getDescription() {
            return form.getDescription();
        }

        public String getFacebook() {
            return form.getFacebook();
        }

        public String getTwitter() {
            return form.getTwitter();
        }

        public String getEmail() {
            return form.getEmail();
        }

        public String getPhoneNumber() {
            return form.getPhoneNumber();
        }

        public String getAddressStreetLine() {
            return form.getAddressStreetLine();
        }

        public String getAddressPostalCode() {
            return form.getAddressPostalCode();
        }

        public String getAddressCity() {
            return form.getAddressCity();
        }

        public String getOther() {
            return form.getOther();
        }


    }

    public static class EditSiteDescriptionFormBuilder {

        private final EditSiteDescriptionForm form;


        protected EditSiteDescriptionFormBuilder() {
            this.form = new EditSiteDescriptionForm();
        }

        public EditSiteDescriptionFormBuilder withSitename(String sitename) {
            form.setSitename(sitename);
            return this;
        }

        public EditSiteDescriptionFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public EditSiteDescriptionFormBuilder withFacebook(String facebook) {
            form.setFacebook(facebook);
            return this;
        }

        public EditSiteDescriptionFormBuilder withTwitter(String twitter) {
            form.setTwitter(twitter);
            return this;
        }

        public EditSiteDescriptionFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public EditSiteDescriptionFormBuilder withPhoneNumber(String phoneNumber) {
            form.setPhoneNumber(phoneNumber);
            return this;
        }

        public EditSiteDescriptionFormBuilder withAddressStreetLine(String addressStreetLine) {
            form.setAddressStreetLine(addressStreetLine);
            return this;
        }

        public EditSiteDescriptionFormBuilder withAddressPostalCode(String addressPostalCode) {
            form.setAddressPostalCode(addressPostalCode);
            return this;
        }

        public EditSiteDescriptionFormBuilder withAddressCity(String addressCity) {
            form.setAddressCity(addressCity);
            return this;
        }

        public EditSiteDescriptionFormBuilder withOther(String other) {
            form.setOther(other);
            return this;
        }

        public EditSiteDescriptionForm get() {
            return form;
        }

    }

}
