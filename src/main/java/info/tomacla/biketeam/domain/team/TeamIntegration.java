package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;

import javax.persistence.*;
import java.util.Objects;


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

    @Column(name = "mattermost_api_token")
    private String mattermostApiToken;
    @Column(name = "mattermost_channel_id")
    private String mattermostChannelID;
    @Column(name = "mattermost_message_channel_id")
    private String mattermostMessageChannelID;
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
    @Column(name = "webhook_ride")
    private String webhookRide;
    @Column(name = "webhook_trip")
    private String webhookTrip;
    @Column(name = "webhook_publication")
    private String webhookPublication;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = Objects.requireNonNull(teamId, "teamId is null");
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = Objects.requireNonNull(team);
        this.teamId = team.getId();
    }

    public boolean isMattermostConfigured() {
        return !Strings.isBlank(this.mattermostApiEndpoint, this.mattermostChannelID, this.mattermostApiToken);
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

    public String getMattermostMessageChannelID() {
        return mattermostMessageChannelID;
    }

    public void setMattermostMessageChannelID(String mattermostMessageChannelID) {
        this.mattermostMessageChannelID = Strings.requireNonBlankOrNull(mattermostMessageChannelID);
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

    public String getWebhookRide() {
        return webhookRide;
    }

    public void setWebhookRide(String webhookRide) {
        this.webhookRide = Strings.requireNonBlankOrNull(webhookRide);
    }

    public String getWebhookTrip() {
        return webhookTrip;
    }

    public void setWebhookTrip(String webhookTrip) {
        this.webhookTrip = Strings.requireNonBlankOrNull(webhookTrip);
    }

    public String getWebhookPublication() {
        return webhookPublication;
    }

    public void setWebhookPublication(String webhookPublication) {
        this.webhookPublication = Strings.requireNonBlankOrNull(webhookPublication);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamIntegration that = (TeamIntegration) o;
        return teamId.equals(that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}