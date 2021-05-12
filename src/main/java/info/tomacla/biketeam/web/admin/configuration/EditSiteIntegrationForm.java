package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.global.SiteIntegration;

public class EditSiteIntegrationForm {

    private String mapBoxAPIKey;

    public String getMapBoxAPIKey() {
        return mapBoxAPIKey;
    }

    public void setMapBoxAPIKey(String mapBoxAPIKey) {
        this.mapBoxAPIKey = Strings.requireNonBlankOrDefault(mapBoxAPIKey, "");
    }

    public static EditSiteIntegrationForm build(SiteIntegration obj) {
        EditSiteIntegrationForm form = new EditSiteIntegrationForm();
        form.mapBoxAPIKey = obj.getMapBoxAPIKey();
        return form;
    }

    public EditSiteIntegrationFormParser parser() {
        return new EditSiteIntegrationFormParser(this);
    }

    public static EditSiteIntegrationFormBuilder builder() {
        return new EditSiteIntegrationFormBuilder();
    }

    public static class EditSiteIntegrationFormParser {

        private final EditSiteIntegrationForm form;

        public EditSiteIntegrationFormParser(EditSiteIntegrationForm form) {
            this.form = form;
        }

        public String getMapBoxAPIKey() {
            return form.getMapBoxAPIKey();
        }

    }

    public static class EditSiteIntegrationFormBuilder {

        private final EditSiteIntegrationForm form;

        public EditSiteIntegrationFormBuilder() {
            this.form = new EditSiteIntegrationForm();
        }

        public EditSiteIntegrationFormBuilder withMapBoxAPIKey(String mapBoxAPIKey) {
            form.setMapBoxAPIKey(mapBoxAPIKey);
            return this;
        }

        public EditSiteIntegrationForm get() {
            return form;
        }
    }

}
