package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.Timezone;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditSiteConfigurationForm {

    private String timezone;
    private List<String> defaultSearchTags;

    public EditSiteConfigurationForm() {
        setTimezone(Timezone.DEFAULT_TIMEZONE);
        setDefaultSearchTags(new ArrayList<>());
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

        public EditSiteConfigurationForm get() {
            return form;
        }

    }

}
