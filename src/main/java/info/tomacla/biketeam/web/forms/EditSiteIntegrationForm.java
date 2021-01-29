package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.domain.global.SiteDescription;
import info.tomacla.biketeam.domain.global.SiteIntegration;

public class EditSiteIntegrationForm {

    private String mapBoxAPIKey;

    public String getMapBoxAPIKey() {
        return mapBoxAPIKey == null ? "" : mapBoxAPIKey;
    }

    public void setMapBoxAPIKey(String mapBoxAPIKey) {
        this.mapBoxAPIKey = mapBoxAPIKey;
    }

    public static EditSiteIntegrationForm build(SiteIntegration obj) {
        EditSiteIntegrationForm form = new EditSiteIntegrationForm();
        form.mapBoxAPIKey = obj.getMapBoxAPIKey();
        return form;
    }

}
