package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.data.Timezone;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditTeamConfigurationForm {

    private String timezone = Timezone.DEFAULT_TIMEZONE;
    private List<String> defaultSearchTags = new ArrayList<>();
    private String feedVisible = null;
    private String reactionVisible = null;

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


    public String getFeedVisible() {
        return feedVisible;
    }

    public void setFeedVisible(String feedVisible) {
        this.feedVisible = feedVisible;
    }

    public String getReactionVisible() {
        return reactionVisible;
    }

    public void setReactionVisible(String reactionVisible) {
        this.reactionVisible = reactionVisible;
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


        public boolean isFeedVisible() {
            return form.getFeedVisible() != null && form.getFeedVisible().equals("on");
        }

        public boolean isReactionVisible() {
            return form.getReactionVisible() != null && form.getReactionVisible().equals("on");
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

        public EditTeamConfigurationFormBuilder withReactionVisible(boolean reactionVisible) {
            form.setReactionVisible(reactionVisible ? "on" : null);
            return this;
        }


        public EditTeamConfigurationForm get() {
            return form;
        }

    }

}
