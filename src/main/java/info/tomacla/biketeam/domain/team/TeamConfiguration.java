package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.datatype.Strings;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "team_configuration")
public class TeamConfiguration {

    @Id
    @Column(name = "team_id")
    private String teamId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "team_id")
    private Team team;
    private String timezone = Timezone.DEFAULT_TIMEZONE;
    @ElementCollection
    @CollectionTable(
            name = "TEAM_CONFIGURATION_DEFAULT_SEARCH_TAGS",
            joinColumns = @JoinColumn(name = "team_configuration_id", referencedColumnName = "team_id")
    )
    private List<String> defaultSearchTags = new ArrayList<>();
    @Column(name = "feed_visible")
    private boolean feedVisible = true;
    @Column
    private String domain;
    @Column(name = "markdown_page")
    private String markdownPage;

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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = Strings.requireNonBlankOrDefault(timezone, Timezone.DEFAULT_TIMEZONE);
    }

    public List<String> getDefaultSearchTags() {
        return defaultSearchTags;
    }

    public void setDefaultSearchTags(List<String> defaultSearchTags) {
        this.defaultSearchTags = Objects.requireNonNullElse(defaultSearchTags, new ArrayList<>());
    }


    public boolean isFeedVisible() {
        return feedVisible;
    }

    public void setFeedVisible(boolean feedVisible) {
        this.feedVisible = feedVisible;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDomainConfigured() {
        return this.domain != null;
    }

    public String getMarkdownPage() {
        return markdownPage;
    }

    public void setMarkdownPage(String markdownPage) {
        this.markdownPage = Strings.requireNonBlankOrNull(markdownPage);
    }

    public boolean isMarkdownPageWritten() {
        return !Strings.isBlank(this.markdownPage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamConfiguration that = (TeamConfiguration) o;
        return teamId.equals(that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}
