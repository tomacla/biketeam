package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.Strings;

public class EditSiteIntegrationForm {

    private String mapBoxAPIKey;
    private String facebookAppId;
    private String facebookAppSecret;
    private String facebookPageId;

    public String getMapBoxAPIKey() {
        return mapBoxAPIKey;
    }

    public void setMapBoxAPIKey(String mapBoxAPIKey) {
        this.mapBoxAPIKey = Strings.requireNonBlankOrDefault(mapBoxAPIKey, "");
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = Strings.requireNonBlankOrDefault(facebookAppId, "");
    }

    public String getFacebookAppSecret() {
        return facebookAppSecret;
    }

    public void setFacebookAppSecret(String facebookAppSecret) {
        this.facebookAppSecret = Strings.requireNonBlankOrDefault(facebookAppSecret, "");
    }

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = Strings.requireNonBlankOrDefault(facebookPageId, "");
    }

    public EditSiteIntegrationFormParser parser() {
        return new EditSiteIntegrationFormParser(this);
    }

    public static EditSiteIntegrationFormBuilder builder() {
        return new EditSiteIntegrationFormBuilder();
    }

    public static class EditSiteIntegrationFormParser {

        private final EditSiteIntegrationForm form;

        public EditSiteIntegrationFormParser(EditSiteIntegrationForm form) {
            this.form = form;
        }

        public String getMapBoxAPIKey() {
            return form.getMapBoxAPIKey();
        }

        public String getFacebookAppId() {
            return form.getFacebookAppId();
        }

        public String getFacebookAppSecret() {
            return form.getFacebookAppSecret();
        }

        public String getFacebookPageId() {
            return form.getFacebookPageId();
        }

    }

    public static class EditSiteIntegrationFormBuilder {

        private final EditSiteIntegrationForm form;

        public EditSiteIntegrationFormBuilder() {
            this.form = new EditSiteIntegrationForm();
        }

        public EditSiteIntegrationFormBuilder withMapBoxAPIKey(String mapBoxAPIKey) {
            form.setMapBoxAPIKey(mapBoxAPIKey);
            return this;
        }

        public EditSiteIntegrationFormBuilder withFacebookAppId(String facebookAppId) {
            form.setFacebookAppId(facebookAppId);
            return this;
        }

        public EditSiteIntegrationFormBuilder withFacebookAppSecret(String facebookAppSecret) {
            form.setFacebookAppSecret(facebookAppSecret);
            return this;
        }

        public EditSiteIntegrationFormBuilder withFacebookPageId(String facebookPageId) {
            form.setFacebookPageId(facebookPageId);
            return this;
        }


        public EditSiteIntegrationForm get() {
            return form;
        }
    }

}
