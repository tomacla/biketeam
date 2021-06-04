package info.tomacla.biketeam.domain.global;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Timezone;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "site_configuration")
public class SiteConfiguration {

    @Id
    private Long id = 1L;
    private String timezone;
    @ElementCollection
    @CollectionTable(
            name = "SITE_CONFIGURATION_DEFAULT_SEARCH_TAGS",
            joinColumns = @JoinColumn(name = "site_configuration_id", referencedColumnName = "id")
    )
    private List<String> defaultSearchTags;
    @Column(name = "default_page")
    @Enumerated(EnumType.STRING)
    private Page defaultPage;
    @Column(name = "feed_visible")
    private boolean feedVisible;
    @Column(name = "rides_visible")
    private boolean ridesVisible;

    protected SiteConfiguration() {

    }

    public SiteConfiguration(String timezone, List<String> defaultSearchTags,
                             Page defaultPage, boolean feedVisible, boolean ridesVisible) {
        setTimezone(timezone);
        setDefaultSearchTags(defaultSearchTags);
        setDefaultPage(defaultPage);
        setFeedVisible(feedVisible);
        setRidesVisible(ridesVisible);
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = 1L;
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

    public Page getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(Page defaultPage) {
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

}
