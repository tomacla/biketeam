package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;


@Entity
@Table(name = "team_integration")
public class TeamIntegration {

    @Id
    @Column(name = "team_id")
    private String teamId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "facebook_page_id")
    private String facebookPageId;
    @Column(name = "facebook_group_details")
    private boolean facebookGroupDetails;
    @Column(name = "facebook_publish_trips")
    private boolean facebookPublishTrips;
    @Column(name = "facebook_publish_rides")
    private boolean facebookPublishRides;
    @Column(name = "facebook_publish_publications")
    private boolean facebookPublishPublications;
    @Column(name = "mattermost_api_token")
    private String mattermostApiToken;
    @Column(name = "mattermost_channel_id")
    private String mattermostChannelID;
    @Column(name = "mattermost_api_endpoint")
    private String mattermostApiEndpoint;
    @Column(name = "mattermost_publish_trips")
    private boolean mattermostPublishTrips;
    @Column(name = "mattermost_publish_rides")
    private boolean mattermostPublishRides;
    @Column(name = "mattermost_publish_publications")
    private boolean mattermostPublishPublications;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "heatmap_center_lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "heatmap_center_lng"))
    })
    @Embedded
    private Point heatmapCenter;
    @Column(name = "heatmap_display")
    private boolean heatmapDisplay;

    public TeamIntegration() {

    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
        this.teamId = team.getId();
    }

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = Strings.requireNonBlankOrNull(facebookPageId);
    }

    public boolean isFacebookGroupDetails() {
        return facebookGroupDetails;
    }

    public void setFacebookGroupDetails(boolean facebookGroupDetails) {
        this.facebookGroupDetails = facebookGroupDetails;
    }

    public boolean isFacebookPublishTrips() {
        return facebookPublishTrips;
    }

    public void setFacebookPublishTrips(boolean facebookPublishTrips) {
        this.facebookPublishTrips = facebookPublishTrips;
    }

    public boolean isFacebookPublishRides() {
        return facebookPublishRides;
    }

    public void setFacebookPublishRides(boolean facebookPublishRides) {
        this.facebookPublishRides = facebookPublishRides;
    }

    public boolean isFacebookPublishPublications() {
        return facebookPublishPublications;
    }

    public void setFacebookPublishPublications(boolean facebookPublishPublications) {
        this.facebookPublishPublications = facebookPublishPublications;
    }

    public boolean isFacebookConfigured() {
        return facebookPageId != null;
    }

    public boolean isMattermostConfigured() {
        return this.mattermostApiEndpoint != null && this.mattermostChannelID != null && this.mattermostApiToken != null;
    }

    public String getMattermostApiToken() {
        return mattermostApiToken;
    }

    public void setMattermostApiToken(String mattermostApiToken) {
        this.mattermostApiToken = Strings.requireNonBlankOrNull(mattermostApiToken);
    }

    public String getMattermostChannelID() {
        return mattermostChannelID;
    }

    public void setMattermostChannelID(String mattermostChannelID) {
        this.mattermostChannelID = Strings.requireNonBlankOrNull(mattermostChannelID);
    }

    public String getMattermostApiEndpoint() {
        return mattermostApiEndpoint;
    }

    public void setMattermostApiEndpoint(String mattermostApiEndpoint) {
        this.mattermostApiEndpoint = Strings.requireNonBlankOrNull(mattermostApiEndpoint);
    }

    public boolean isMattermostPublishTrips() {
        return mattermostPublishTrips;
    }

    public void setMattermostPublishTrips(boolean mattermostPublishTrips) {
        this.mattermostPublishTrips = mattermostPublishTrips;
    }

    public boolean isMattermostPublishRides() {
        return mattermostPublishRides;
    }

    public void setMattermostPublishRides(boolean mattermostPublishRides) {
        this.mattermostPublishRides = mattermostPublishRides;
    }

    public boolean isMattermostPublishPublications() {
        return mattermostPublishPublications;
    }

    public void setMattermostPublishPublications(boolean mattermostPublishPublications) {
        this.mattermostPublishPublications = mattermostPublishPublications;
    }

    public Point getHeatmapCenter() {
        return heatmapCenter;
    }

    public void setHeatmapCenter(Point heatmapCenter) {
        this.heatmapCenter = heatmapCenter;
    }

    public boolean isHeatmapDisplay() {
        return heatmapDisplay;
    }

    public void setHeatmapDisplay(boolean heatmapDisplay) {
        this.heatmapDisplay = heatmapDisplay;
    }

    public boolean isHeatmapConfigured() {
        return this.heatmapCenter != null;
    }

}