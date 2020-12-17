package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.navigationmap.Map;

public class NewMapForm {

    private String id;
    private String name;
    private double length;
    private double positiveElevation;
    private double negativeElevation;
    private boolean crossing;
    private boolean visible;
    private String tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getPositiveElevation() {
        return positiveElevation;
    }

    public void setPositiveElevation(double positiveElevation) {
        this.positiveElevation = positiveElevation;
    }

    public double getNegativeElevation() {
        return negativeElevation;
    }

    public void setNegativeElevation(double negativeElevation) {
        this.negativeElevation = negativeElevation;
    }

    public boolean isCrossing() {
        return crossing;
    }

    public void setCrossing(boolean crossing) {
        this.crossing = crossing;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public static NewMapForm build(Map obj) {
        NewMapForm form = new NewMapForm();
        form.id = obj.getId();
        form.name = obj.getName();
        form.length = obj.getLength();
        form.crossing = obj.isCrossing();
        form.visible = obj.isVisible();
        form.negativeElevation = obj.getNegativeElevation();
        form.positiveElevation = obj.getPositiveElevation();
        form.tags = Json.serialize(obj.getTags());
        return form;
    }

}
