package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Json;

import java.util.List;

public class SearchMapForm {

    private double lowerDistance;
    private double upperDistance;
    private String sort;
    private String windDirection;
    private String tags;

    public double getLowerDistance() {
        return lowerDistance;
    }

    public void setLowerDistance(double lowerDistance) {
        this.lowerDistance = lowerDistance;
    }

    public double getUpperDistance() {
        return upperDistance;
    }

    public void setUpperDistance(double upperDistance) {
        this.upperDistance = upperDistance;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public static SearchMapForm empty(List<String> defaultSearchTags) {
        SearchMapForm form = new SearchMapForm();
        form.setLowerDistance(1);
        form.setUpperDistance(1000);
        form.setTags(Json.serialize(defaultSearchTags));
        form.setWindDirection("");
        form.setSort("");
        return form;
    }

}
