package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.common.datatype.Strings;

public class EditUserForm {

    private String stravaId = "";
    private String email = "";
    private String emailPublishTrips = null;
    private String emailPublishRides = null;
    private String emailPublishPublications = null;

    public static EditUserFormBuilder builder() {
        return new EditUserFormBuilder();
    }

    public String getStravaId() {
        return stravaId;
    }

    public void setStravaId(String stravaId) {
        this.stravaId = Strings.requireNonBlankOrDefault(stravaId, "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrDefault(email, "");
    }

    public String getEmailPublishTrips() {
        return emailPublishTrips;
    }

    public void setEmailPublishTrips(String emailPublishTrips) {
        this.emailPublishTrips = emailPublishTrips;
    }

    public String getEmailPublishRides() {
        return emailPublishRides;
    }

    public void setEmailPublishRides(String emailPublishRides) {
        this.emailPublishRides = emailPublishRides;
    }

    public String getEmailPublishPublications() {
        return emailPublishPublications;
    }

    public void setEmailPublishPublications(String emailPublishPublications) {
        this.emailPublishPublications = emailPublishPublications;
    }

    public EditUserFormParser parser() {
        return new EditUserFormParser(this);
    }

    public static class EditUserFormParser {

        private final EditUserForm form;

        public EditUserFormParser(EditUserForm form) {
            this.form = form;
        }

        public Long getStravaId() {
            if (Strings.isBlank(form.getStravaId())) {
                return null;
            }
            return Long.valueOf(form.getStravaId());
        }

        public String getEmail() {
            return Strings.requireNonBlankOrNull(form.getEmail());
        }

        public boolean isEmailPublishRides() {
            return form.getEmailPublishRides() != null && form.getEmailPublishRides().equals("on");
        }

        public boolean isEmailPublishTrips() {
            return form.getEmailPublishTrips() != null && form.getEmailPublishTrips().equals("on");
        }

        public boolean isEmailPublishPublications() {
            return form.getEmailPublishPublications() != null && form.getEmailPublishPublications().equals("on");
        }

    }

    public static class EditUserFormBuilder {

        private final EditUserForm form;

        public EditUserFormBuilder() {
            this.form = new EditUserForm();
        }

        public EditUserFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public EditUserFormBuilder withStravaId(Long stravaId) {
            form.setStravaId(stravaId != null ? String.valueOf(stravaId) : null);
            return this;
        }

        public EditUserFormBuilder withEmailPublishRides(boolean emailPublishRides) {
            form.setEmailPublishRides(emailPublishRides ? "on" : null);
            return this;
        }

        public EditUserFormBuilder withEmailPublishTrips(boolean emailPublishTrips) {
            form.setEmailPublishTrips(emailPublishTrips ? "on" : null);
            return this;
        }

        public EditUserFormBuilder withEmailPublishPublications(boolean emailPublishPublications) {
            form.setEmailPublishPublications(emailPublishPublications ? "on" : null);
            return this;
        }

        public EditUserForm get() {
            return form;
        }
    }

}
