package info.tomacla.biketeam.api.dto;

import java.util.List;

public class AndroidMapDTO {

    private String id;
    private String title;
    private String type;
    private List<String> tags;
    private long time;
    private double distance;
    private long positiveElevation;
    private long negativeElevation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getPositiveElevation() {
        return positiveElevation;
    }

    public void setPositiveElevation(long positiveElevation) {
        this.positiveElevation = positiveElevation;
    }

    public long getNegativeElevation() {
        return negativeElevation;
    }

    public void setNegativeElevation(long negativeElevation) {
        this.negativeElevation = negativeElevation;
    }

}
