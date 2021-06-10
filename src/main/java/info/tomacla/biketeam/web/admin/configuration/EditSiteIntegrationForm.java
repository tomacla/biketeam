package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.Strings;

public class EditSiteIntegrationForm {

    private String mapBoxAPIKey;
    private String facebookAppId;
    private String facebookAppSecret;
    private String facebookPageId;
    private String smtpHost;
    private String smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String smtpFrom;

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

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = Strings.requireNonBlankOrDefault(smtpHost, "");
        ;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = Strings.requireNonBlankOrDefault(smtpPort, "");
        ;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = Strings.requireNonBlankOrDefault(smtpUser, "");
        ;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = Strings.requireNonBlankOrDefault(smtpPassword, "");
        ;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = Strings.requireNonBlankOrDefault(smtpFrom, "");
        ;
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

        public String getSmtpHost() {
            return form.getSmtpHost();
        }

        public String getSmtpPort() {
            return form.getSmtpPort();
        }

        public String getSmtpUser() {
            return form.getSmtpUser();
        }

        public String getSmtpPassword() {
            return form.getSmtpPassword();
        }

        public String getSmtpFrom() {
            return form.getSmtpFrom();
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

        public EditSiteIntegrationFormBuilder withSmtpHost(String smtpHost) {
            form.setSmtpHost(smtpHost);
            return this;
        }

        public EditSiteIntegrationFormBuilder withSmtpPort(String smtpPort) {
            form.setSmtpPort(smtpPort);
            return this;
        }

        public EditSiteIntegrationFormBuilder withSmtpUser(String smtpUser) {
            form.setSmtpUser(smtpUser);
            return this;
        }

        public EditSiteIntegrationFormBuilder withSmtpPassword(String smtpPassword) {
            form.setSmtpPassword(smtpPassword);
            return this;
        }

        public EditSiteIntegrationFormBuilder withSmtpFrom(String smtpFrom) {
            form.setSmtpFrom(smtpFrom);
            return this;
        }


        public EditSiteIntegrationForm get() {
            return form;
        }
    }

}
