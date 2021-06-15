package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.Strings;

public class EditTeamIntegrationForm {

    private String facebookPageId;

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = Strings.requireNonBlankOrDefault(facebookPageId, "");
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

        public EditTeamIntegrationForm get() {
            return form;
        }
    }

}
