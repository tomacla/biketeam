package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.team.WebPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditTeamConfigurationForm {

    private String timezone;
    private List<String> defaultSearchTags;
    private String defaultPage;
    private String feedVisible;
    private String ridesVisible;
    private String markdownPage;

    public EditTeamConfigurationForm() {
        setTimezone(Timezone.DEFAULT_TIMEZONE);
        setDefaultSearchTags(new ArrayList<>());
        setDefaultPage("");
        setFeedVisible(null);
        setRidesVisible(null);
        setMarkdownPage(null);
    }

    public static EditTeamConfigurationFormBuilder builder() {
        return new EditTeamConfigurationFormBuilder();
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = Objects.requireNonNullElse(timezone, Timezone.DEFAULT_TIMEZONE);
    }

    public List<String> getDefaultSearchTags() {
        return defaultSearchTags;
    }

    public void setDefaultSearchTags(List<String> defaultSearchTags) {
        this.defaultSearchTags = Objects.requireNonNullElse(defaultSearchTags, new ArrayList<>());
    }

    public String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = Objects.requireNonNullElse(defaultPage, WebPage.FEED.name());
    }

    public String getFeedVisible() {
        return feedVisible;
    }

    public void setFeedVisible(String feedVisible) {
        this.feedVisible = feedVisible;
    }

    public String getRidesVisible() {
        return ridesVisible;
    }

    public void setRidesVisible(String ridesVisible) {
        this.ridesVisible = ridesVisible;
    }

    public String getMarkdownPage() {
        return markdownPage;
    }

    public void setMarkdownPage(String markdownPage) {
        this.markdownPage = Strings.requireNonBlankOrDefault(markdownPage, "");
    }

    public EditTeamConfigurationFormParser parser() {
        return new EditTeamConfigurationFormParser(this);
    }

    public static class EditTeamConfigurationFormParser {

        private final EditTeamConfigurationForm form;

        protected EditTeamConfigurationFormParser(EditTeamConfigurationForm form) {
            this.form = form;
        }

        public String getTimezone() {
            return form.getTimezone();
        }

        public List<String> getDefaultSearchTags() {
            return form.getDefaultSearchTags();
        }

        public WebPage getDefaultPage() {
            return WebPage.valueOf(form.getDefaultPage());
        }

        public boolean isFeedVisible() {
            return form.getFeedVisible() != null && form.getFeedVisible().equals("on");
        }

        public boolean isRidesVisible() {
            return form.getRidesVisible() != null && form.getRidesVisible().equals("on");
        }

        public String getMarkdownPage() {
            return form.getMarkdownPage();
        }

    }

    public static class EditTeamConfigurationFormBuilder {

        private final EditTeamConfigurationForm form;

        protected EditTeamConfigurationFormBuilder() {
            this.form = new EditTeamConfigurationForm();
        }

        public EditTeamConfigurationFormBuilder withTimezone(String timezone) {
            form.setTimezone(timezone);
            return this;
        }

        public EditTeamConfigurationFormBuilder withDefaultSearchTags(List<String> tags) {
            if (tags != null) {
                form.setDefaultSearchTags(tags);
            }
            return this;
        }

        public EditTeamConfigurationFormBuilder withFeedVisible(boolean feedVisible) {
            form.setFeedVisible(feedVisible ? "on" : null);
            return this;
        }

        public EditTeamConfigurationFormBuilder withRidesVisible(boolean ridesVisible) {
            form.setRidesVisible(ridesVisible ? "on" : null);
            return this;
        }

        public EditTeamConfigurationFormBuilder withDefaultPage(WebPage defaultPage) {
            form.setDefaultPage(defaultPage.name());
            return this;
        }

        public EditTeamConfigurationFormBuilder withMarkdownPage(String markdownPage) {
            form.setMarkdownPage(markdownPage);
            return this;
        }

        public EditTeamConfigurationForm get() {
            return form;
        }

    }

}
