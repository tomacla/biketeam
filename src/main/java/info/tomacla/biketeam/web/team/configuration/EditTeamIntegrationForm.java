package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.Strings;

public class EditTeamIntegrationForm {

    private String facebookPageId;
    private String facebookGroupDetails;

    public EditTeamIntegrationForm() {
        setFacebookPageId(null);
        setFacebookGroupDetails(null);
    }

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = Strings.requireNonBlankOrDefault(facebookPageId, "");
    }

    public String getFacebookGroupDetails() {
        return facebookGroupDetails;
    }

    public void setFacebookGroupDetails(String facebookGroupDetails) {
        this.facebookGroupDetails = facebookGroupDetails;
    }

    public EditTeamIntegrationFormParser parser() {
        return new EditTeamIntegrationFormParser(this);
    }

    public static EditTeamIntegrationFormBuilder builder() {
        return new EditTeamIntegrationFormBuilder();
    }

    public static class EditTeamIntegrationFormParser {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormParser(EditTeamIntegrationForm form) {
            this.form = form;
        }

        public String getFacebookPageId() {
            return form.getFacebookPageId();
        }

        public boolean isFacebookGroupDetails() {
            return form.getFacebookGroupDetails() != null && form.getFacebookGroupDetails().equals("on");
        }

    }

    public static class EditTeamIntegrationFormBuilder {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormBuilder() {
            this.form = new EditTeamIntegrationForm();
        }

        public EditTeamIntegrationFormBuilder withFacebookPageId(String facebookPageId) {
            form.setFacebookPageId(facebookPageId);
            return this;
        }

        public EditTeamIntegrationFormBuilder withFacebookGroupDetails(boolean facebookGroupDetails) {
            form.setFacebookGroupDetails(facebookGroupDetails ? "on" : null);
            return this;
        }

        public EditTeamIntegrationForm get() {
            return form;
        }
    }

}
