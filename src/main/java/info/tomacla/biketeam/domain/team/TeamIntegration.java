package info.tomacla.biketeam.domain.team;

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

    @Column(name = "facebook_access_token")
    private String facebookAccessToken;
    @Column(name = "facebook_page_id")
    private String facebookPageId;
    @Column(name = "facebook_group_details")
    private boolean facebookGroupDetails;

    public TeamIntegration() {

    }

    public TeamIntegration(String facebookAccessToken,
                           String facebookPageId) {
        setFacebookAccessToken(facebookAccessToken);
        setFacebookPageId(facebookPageId);
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

    public String getFacebookAccessToken() {
        return facebookAccessToken;
    }

    public void setFacebookAccessToken(String facebookAccessToken) {
        this.facebookAccessToken = Strings.requireNonBlankOrNull(facebookAccessToken);
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
        return getFacebookConfigurationStep() == 3;
    }

    public int getFacebookConfigurationStep() {
        if (facebookAccessToken != null && facebookPageId != null) {
            return 3;
        }
        if (facebookAccessToken != null) {
            return 2;
        }
        return 1;
    }

}