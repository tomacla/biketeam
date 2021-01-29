package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.global.SiteConfiguration;
import info.tomacla.biketeam.domain.global.SiteIntegration;

public class EditSiteConfigurationForm {

    private boolean soundEnabled;
    private String timezone;

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone == null ? Timezone.DEFAULT_TIMEZONE : timezone;
    }


    public static EditSiteConfigurationForm build(SiteConfiguration obj) {
        EditSiteConfigurationForm form = new EditSiteConfigurationForm();
        form.soundEnabled = obj.isSoundEnabled();
        form.timezone = obj.getTimezone();
        return form;
    }

}
