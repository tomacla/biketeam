package info.tomacla.biketeam.web.team.configuration;

import java.util.Objects;

public class EditTeamDescriptionForm {

    private String description;
    private String facebook;
    private String twitter;
    private String email;
    private String phoneNumber;
    private String addressStreetLine;
    private String addressPostalCode;
    private String addressCity;
    private String other;

    public EditTeamDescriptionForm() {
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

    public EditTeamDescriptionFormParser parser() {
        return new EditTeamDescriptionFormParser(this);
    }

    public static EditTeamDescriptionFormBuilder builder() {
        return new EditTeamDescriptionFormBuilder();
    }

    public static class EditTeamDescriptionFormParser {

        private final EditTeamDescriptionForm form;

        protected EditTeamDescriptionFormParser(EditTeamDescriptionForm form) {
            this.form = form;
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

    public static class EditTeamDescriptionFormBuilder {

        private final EditTeamDescriptionForm form;


        protected EditTeamDescriptionFormBuilder() {
            this.form = new EditTeamDescriptionForm();
        }


        public EditTeamDescriptionFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public EditTeamDescriptionFormBuilder withFacebook(String facebook) {
            form.setFacebook(facebook);
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
