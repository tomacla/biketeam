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
    @Column(name = "mattermost_api_token")
    private String mattermostApiToken;
    @Column(name = "mattermost_channel_id")
    private String mattermostChannelID;
    @Column(name = "mattermost_api_endpoint")
    private String mattermostApiEndpoint;
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