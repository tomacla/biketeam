package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;
import org.springframework.util.ObjectUtils;

public class EditTeamIntegrationForm {

    private String mattermostApiToken = "";
    private String mattermostChannelID = "";
    private String mattermostMessageChannelID = "";
    private String mattermostApiEndpoint = "";
    private String mattermostPublishTrips = null;
    private String mattermostPublishRides = null;
    private String mattermostPublishPublications = null;
    private String heatmapCenterLat = "";
    private String heatmapCenterLng = "";
    private String heatmapDisplay = null;

    public static EditTeamIntegrationFormBuilder builder() {
        return new EditTeamIntegrationFormBuilder();
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

    public String getMattermostMessageChannelID() {
        return mattermostMessageChannelID;
    }

    public void setMattermostMessageChannelID(String mattermostMessageChannelID) {
        this.mattermostMessageChannelID = Strings.requireNonBlankOrDefault(mattermostMessageChannelID, "");
    }

    public String getMattermostApiEndpoint() {
        return mattermostApiEndpoint;
    }

    public void setMattermostApiEndpoint(String mattermostApiEndpoint) {
        this.mattermostApiEndpoint = Strings.requireNonBlankOrDefault(mattermostApiEndpoint, "");
    }

    public String getMattermostPublishTrips() {
        return mattermostPublishTrips;
    }

    public void setMattermostPublishTrips(String mattermostPublishTrips) {
        this.mattermostPublishTrips = mattermostPublishTrips;
    }

    public String getMattermostPublishRides() {
        return mattermostPublishRides;
    }

    public void setMattermostPublishRides(String mattermostPublishRides) {
        this.mattermostPublishRides = mattermostPublishRides;
    }

    public String getMattermostPublishPublications() {
        return mattermostPublishPublications;
    }

    public void setMattermostPublishPublications(String mattermostPublishPublications) {
        this.mattermostPublishPublications = mattermostPublishPublications;
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

    public String getHeatmapDisplay() {
        return heatmapDisplay;
    }

    public void setHeatmapDisplay(String heatmapDisplay) {
        this.heatmapDisplay = heatmapDisplay;
    }


    public EditTeamIntegrationFormParser parser() {
        return new EditTeamIntegrationFormParser(this);
    }

    public static class EditTeamIntegrationFormParser {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormParser(EditTeamIntegrationForm form) {
            this.form = form;
        }

        public String getMattermostApiToken() {
            return Strings.requireNonBlankOrNull(form.getMattermostApiToken());
        }

        public String getMattermostChannelID() {
            return Strings.requireNonBlankOrNull(form.getMattermostChannelID());
        }

        public String getMattermostMessageChannelID() {
            return Strings.requireNonBlankOrNull(form.getMattermostMessageChannelID());
        }

        public String getMattermostApiEndpoint() {
            return Strings.requireNonBlankOrNull(form.getMattermostApiEndpoint());
        }

        public boolean isMattermostPublishRides() {
            return form.getMattermostPublishRides() != null && form.getMattermostPublishRides().equals("on");
        }

        public boolean isMattermostPublishTrips() {
            return form.getMattermostPublishTrips() != null && form.getMattermostPublishTrips().equals("on");
        }

        public boolean isMattermostPublishPublications() {
            return form.getMattermostPublishPublications() != null && form.getMattermostPublishPublications().equals("on");
        }

        public Point getHeatmapCenter() {
            if (!ObjectUtils.isEmpty(form.getHeatmapCenterLat()) && !ObjectUtils.isEmpty(form.getHeatmapCenterLng())) {
                return new Point(Double.parseDouble(form.getHeatmapCenterLat()), Double.parseDouble(form.getHeatmapCenterLng()));
            }
            return null;
        }

        public boolean isHeatmapDisplay() {
            return form.getHeatmapDisplay() != null && form.getHeatmapDisplay().equals("on");
        }


    }

    public static class EditTeamIntegrationFormBuilder {

        private final EditTeamIntegrationForm form;

        public EditTeamIntegrationFormBuilder() {
            this.form = new EditTeamIntegrationForm();
        }

        public EditTeamIntegrationFormBuilder withMattermostApiToken(String mattermostApiToken) {
            form.setMattermostApiToken(mattermostApiToken);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostChannelID(String mattermostChannelID) {
            form.setMattermostChannelID(mattermostChannelID);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostMessageChannelID(String mattermostMessageChannelID) {
            form.setMattermostMessageChannelID(mattermostMessageChannelID);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostPublishRides(boolean mattermostPublishRides) {
            form.setMattermostPublishRides(mattermostPublishRides ? "on" : null);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostPublishTrips(boolean mattermostPublishTrips) {
            form.setMattermostPublishTrips(mattermostPublishTrips ? "on" : null);
            return this;
        }

        public EditTeamIntegrationFormBuilder withMattermostPublishPublications(boolean mattermostPublishPublications) {
            form.setMattermostPublishPublications(mattermostPublishPublications ? "on" : null);
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

        public EditTeamIntegrationFormBuilder withHeatmapDisplay(boolean heatmapDisplay) {
            form.setHeatmapDisplay(heatmapDisplay ? "on" : null);
            return this;
        }

        public EditTeamIntegrationForm get() {
            return form;
        }


    }

}
