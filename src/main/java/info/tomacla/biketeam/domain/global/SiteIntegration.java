package info.tomacla.biketeam.domain.global;

import info.tomacla.biketeam.common.Strings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "site_integration")
public class SiteIntegration {

    @Id
    private Long id = 1L;
    @Column(name = "map_box_api_key")
    private String mapBoxAPIKey;
    @Column(name = "facebook_app_id")
    private String facebookAppId;
    @Column(name = "facebook_app_secret")
    private String facebookAppSecret;
    @Column(name = "facebook_access_token")
    private String facebookAccessToken;
    @Column(name = "facebook_page_id")
    private String facebookPageId;
    @Column(name = "smtp_host")
    private String smtpHost;
    @Column(name = "smtp_port")
    private String smtpPort;
    @Column(name = "smtp_user")
    private String smtpUser;
    @Column(name = "smtp_password")
    private String smtpPassword;
    @Column(name = "smtp_from")
    private String smtpFrom;

    public SiteIntegration() {

    }

    public SiteIntegration(String mapBoxAPIKey,
                           String facebookAppId,
                           String facebookAppSecret,
                           String facebookAccessToken,
                           String facebookPageId) {
        setMapBoxAPIKey(mapBoxAPIKey);
        setFacebookAppId(facebookAppId);
        setFacebookAppSecret(facebookAppSecret);
        setFacebookAccessToken(facebookAccessToken);
        setFacebookPageId(facebookPageId);
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = 1L;
    }

    public String getMapBoxAPIKey() {
        return mapBoxAPIKey;
    }

    public void setMapBoxAPIKey(String mapBoxAPIKey) {
        this.mapBoxAPIKey = Strings.requireNonBlankOrNull(mapBoxAPIKey);
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = Strings.requireNonBlankOrNull(facebookAppId);
    }

    public String getFacebookAppSecret() {
        return facebookAppSecret;
    }

    public void setFacebookAppSecret(String facebookAppSecret) {
        this.facebookAppSecret = Strings.requireNonBlankOrNull(facebookAppSecret);
    }

    public String getFacebookAccessToken() {
        return facebookAccessToken;
    }

    public void setFacebookAccessToken(String facebookAccessToken) {
        this.facebookAccessToken = Strings.requireNonBlankOrNull(facebookAccessToken);
    }

    public String getFacebookPageId() {
        return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = Strings.requireNonBlankOrNull(facebookPageId);
    }

    public boolean isFacebookConfigured() {
        return getFacebookConfigurationStep() == 4;
    }

    public int getFacebookConfigurationStep() {
        if (facebookAppId != null && facebookAppSecret != null && facebookAccessToken != null && facebookPageId != null) {
            return 4;
        }
        if (facebookAppId != null && facebookAppSecret != null && facebookAccessToken != null) {
            return 3;
        }
        if (facebookAppId != null && facebookAppSecret != null) {
            return 2;
        }
        return 1;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = Strings.requireNonBlankOrNull(smtpHost);
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = Strings.requireNonBlankOrNull(smtpPort);
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = Strings.requireNonBlankOrNull(smtpUser);
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = Strings.requireNonBlankOrNull(smtpPassword);
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = Strings.requireNonBlankOrNull(smtpFrom);
    }

    public boolean isSmtpConfigured() {
        return smtpHost != null && smtpFrom != null && smtpUser != null && smtpPassword != null && smtpPort != null;
    }

}