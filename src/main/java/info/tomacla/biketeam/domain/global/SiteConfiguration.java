package info.tomacla.biketeam.domain.global;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Timezone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "site_configuration")
public class SiteConfiguration {

    @Id
    private Long id = 1L;
    private String timezone;
    @Column(name = "sound_enabled")
    private boolean soundEnabled;

    protected SiteConfiguration() {

    }

    public SiteConfiguration(String timezone, boolean soundEnabled) {
        setTimezone(timezone);
        setSoundEnabled(soundEnabled);
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

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
}
