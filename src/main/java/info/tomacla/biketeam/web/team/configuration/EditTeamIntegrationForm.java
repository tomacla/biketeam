package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Strings;
import org.springframework.util.ObjectUtils;

public class EditTeamIntegrationForm {

    private String facebookGroupDetails;
    private String mattermostApiToken;
    private String mattermostChannelID;
    private String mattermostApiEndpoint;
    private String heatmapCenterLat;
    private String heatmapCenterLng;

    public EditTeamIntegrationForm() {
        setFacebookGroupDetails(null);
        setMattermostApiEndpoint(null);
        setMattermostApiToken(null);
        setMattermostChannelID(null);
        setHeatmapCenterLat(null);
        setHeatmapCenterLng(null);
    }

    public static EditTeamIntegrationFormBuilder builder() {
        return new EditTeamIntegrationFormBuilder();
    }

    public String getFacebookGroupDetails() {
        return facebookGroupDetails;
    }

    public void setFacebookGroupDetails(String facebookGroupDetails) {
        this.facebookGroupDetails = facebookGroupDetails;
    }

    public String getMattermostApiToken() {
        return mattermostApiToken;
    }

    public void setMattermostApiToken(String mattermostApiToken) {
        this.mattermostApiToken = Strings.requireNonBlankOrDefault(mattermostApiToken, "");
    }

    public String getMattermostChannelID() {
        return mattermostChannelID;
    }

    public void setMattermostChannelID(String mattermostChannelID) {
        this.mattermostChannelID = Strings.requireNonBlankOrDefault(mattermostChannelID, "");
    }

    public String getMattermostApiEndpoint() {
        return mattermostApiEndpoint;
    }

    public void setMattermostApiEndpoint(String mattermostApiEndpoint) {
        this.mattermostApiEndpoint = Strings.requireNonBlankOrDefault(mattermostApiEndpoint, "");
    }

    public String getHeatmapCenterLat() {
        return heatmapCenterLat;
    }

    public void setHeatmapCenterLat(String heatmapCenterLat) {
        this.heatmapCenterLat = Strings.requireNonBlankOrDefault(heatmapCenterLat, "");
    }

    public String getHeatmapCenterLng() {
        return heatmapCenterLng;
    }

    public void setHeatmapCenterLng(String heatmapCenterLng) {
        this.heatmapCenterLng = Strings.requireNonBlankOrDefault(heatmapCenterLng, "");
    }

    public EditTeamIntegrationFormParser parser() {
        return new EditTeamIntegrationFormParser(this);
    }

    public static class EditTeamIntegrationFormParser {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormParser(EditTeamIntegrationForm form) {
            this.form = form;
        }

        public boolean isFacebookGroupDetails() {
            return form.getFacebookGroupDetails() != null && form.getFacebookGroupDetails().equals("on");
        }

        public String getMattermostApiToken() {
            return form.getMattermostApiToken();
        }

        public String getMattermostChannelID() {
            return form.getMattermostChannelID();
        }

        public String getMattermostApiEndpoint() {
            return form.getMattermostApiEndpoint();
        }

        public Point getHeatmapCenter() {
            if (!ObjectUtils.isEmpty(form.getHeatmapCenterLat()) && !ObjectUtils.isEmpty(form.getHeatmapCenterLng())) {
                return new Point(Double.parseDouble(form.getHeatmapCenterLat()), Double.parseDouble(form.getHeatmapCenterLng()));
            }
            return null;
        }

    }

    public static class EditTeamIntegrationFormBuilder {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormBuilder() {
            this.form = new EditTeamIntegrationForm();
        }

        public EditTeamIntegrationFormBuilder withFacebookGroupDetails(boolean facebookGroupDetails) {
            form.setFacebookGroupDetails(facebookGroupDetails ? "on" : null);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostApiToken(String mattermostApiToken) {
            form.setMattermostApiToken(mattermostApiToken);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostChannelID(String mattermostChannelID) {
            form.setMattermostChannelID(mattermostChannelID);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostApiEndpoint(String mattermostApiEndpoint) {
            form.setMattermostApiEndpoint(mattermostApiEndpoint);
            return this;
        }

        public EditTeamIntegrationFormBuilder withHeatmapCenter(Point heatmapCenter) {
            if (heatmapCenter != null) {
                form.setHeatmapCenterLat(String.valueOf(heatmapCenter.getLat()));
                form.setHeatmapCenterLng(String.valueOf(heatmapCenter.getLng()));
            }
            return this;
        }

        public EditTeamIntegrationForm get() {
            return form;
        }
    }

}
