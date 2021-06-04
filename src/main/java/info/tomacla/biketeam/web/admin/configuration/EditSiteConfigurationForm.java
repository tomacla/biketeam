package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.global.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditSiteConfigurationForm {

    private String timezone;
    private List<String> defaultSearchTags;
    private String defaultPage;
    private String feedVisible;
    private String ridesVisible;

    public EditSiteConfigurationForm() {
        setTimezone(Timezone.DEFAULT_TIMEZONE);
        setDefaultSearchTags(new ArrayList<>());
        setDefaultPage("");
        setFeedVisible(null);
        setRidesVisible(null);
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
        this.defaultPage = Objects.requireNonNullElse(defaultPage, Page.FEED.name());
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

    public EditSiteConfigurationFormParser parser() {
        return new EditSiteConfigurationFormParser(this);
    }

    public static EditSiteConfigurationFormBuilder builder() {
        return new EditSiteConfigurationFormBuilder();
    }

    public static class EditSiteConfigurationFormParser {

        private final EditSiteConfigurationForm form;

        protected EditSiteConfigurationFormParser(EditSiteConfigurationForm form) {
            this.form = form;
        }

        public String getTimezone() {
            return form.getTimezone();
        }

        public List<String> getDefaultSearchTags() {
            return form.getDefaultSearchTags();
        }

        public Page getDefaultPage() {
            return Page.valueOf(form.getDefaultPage());
        }

        public boolean isFeedVisible() {
            return form.getFeedVisible() != null && form.getFeedVisible().equals("on");
        }

        public boolean isRidesVisible() {
            return form.getRidesVisible() != null && form.getRidesVisible().equals("on");
        }

    }

    public static class EditSiteConfigurationFormBuilder {

        private final EditSiteConfigurationForm form;

        protected EditSiteConfigurationFormBuilder() {
            this.form = new EditSiteConfigurationForm();
        }

        public EditSiteConfigurationFormBuilder withTimezone(String timezone) {
            form.setTimezone(timezone);
            return this;
        }

        public EditSiteConfigurationFormBuilder withDefaultSearchTags(List<String> tags) {
            if (tags != null) {
                form.setDefaultSearchTags(tags);
            }
            return this;
        }

        public EditSiteConfigurationFormBuilder withFeedVisible(boolean feedVisible) {
            form.setFeedVisible(feedVisible ? "on" : null);
            return this;
        }

        public EditSiteConfigurationFormBuilder withRidesVisible(boolean ridesVisible) {
            form.setRidesVisible(ridesVisible ? "on" : null);
            return this;
        }

        public EditSiteConfigurationFormBuilder withDefaultPage(Page defaultPage) {
            form.setDefaultPage(defaultPage.name());
            return this;
        }

        public EditSiteConfigurationForm get() {
            return form;
        }

    }

}
