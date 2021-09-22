package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Timezone;
import org.springframework.util.ObjectUtils;

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

    private String timezone;
    @ElementCollection
    @CollectionTable(
            name = "TEAM_CONFIGURATION_DEFAULT_SEARCH_TAGS",
            joinColumns = @JoinColumn(name = "team_configuration_id", referencedColumnName = "team_id")
    )
    private List<String> defaultSearchTags;
    @Column(name = "default_page")
    @Enumerated(EnumType.STRING)
    private WebPage defaultPage;
    @Column(name = "feed_visible")
    private boolean feedVisible;
    @Column(name = "rides_visible")
    private boolean ridesVisible;
    @Column
    private String domain;
    @Column(name = "markdown_page")
    private String markdownPage;

    protected TeamConfiguration() {

    }

    public TeamConfiguration(String timezone, List<String> defaultSearchTags,
                             WebPage defaultPage, boolean feedVisible, boolean ridesVisible) {
        setTimezone(timezone);
        setDefaultSearchTags(defaultSearchTags);
        setDefaultPage(defaultPage);
        setFeedVisible(feedVisible);
        setRidesVisible(ridesVisible);
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
        this.teamId = team.getId();
        this.team = team;
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

    public WebPage getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(WebPage defaultPage) {
        this.defaultPage = Objects.requireNonNull(defaultPage);
    }

    public boolean isFeedVisible() {
        return feedVisible;
    }

    public void setFeedVisible(boolean feedVisible) {
        this.feedVisible = feedVisible;
    }

    public boolean isRidesVisible() {
        return ridesVisible;
    }

    public void setRidesVisible(boolean ridesVisible) {
        this.ridesVisible = ridesVisible;
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
        return this.markdownPage != null && !this.markdownPage.trim().equals("");
    }

}
