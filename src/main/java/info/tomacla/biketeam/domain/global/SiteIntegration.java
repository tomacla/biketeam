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

    protected SiteIntegration() {

    }

    public SiteIntegration(String mapBoxAPIKey) {
        setMapBoxAPIKey(mapBoxAPIKey);
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

}