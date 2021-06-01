package info.tomacla.biketeam.web.admin.map;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.map.MapType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewMapForm {

    private String id;
    private String name;
    private String type;
    private boolean visible;
    private List<String> tags;

    public NewMapForm() {
        setId(null);
        setName(null);
        setType(null);
        setVisible(false);
        setTags(null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlankOrDefault(name, "");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Strings.requireNonBlankOrDefault(type, MapType.ROAD.name());
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
    }

    public NewMapFormParser parser() {
        return new NewMapFormParser(this);
    }

    public static NewMapFormBuilder builder() {
        return new NewMapFormBuilder();
    }

    public static class NewMapFormParser {

        private final NewMapForm form;

        public NewMapFormParser(NewMapForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getName() {
            return form.getName();
        }

        public boolean isVisible() {
            return form.isVisible();
        }

        public List<String> getTags() {
            return form.getTags();
        }

        public MapType getType() {
            return MapType.valueOf(form.getType());
        }

    }

    public static class NewMapFormBuilder {

        private final NewMapForm form;

        public NewMapFormBuilder() {
            this.form = new NewMapForm();
        }

        public NewMapFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewMapFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewMapFormBuilder withVisible(boolean visible) {
            form.setVisible(visible);
            return this;
        }

        public NewMapFormBuilder withTags(List<String> tags) {
            form.setTags(tags);
            return this;
        }

        public NewMapFormBuilder withType(MapType type) {
            form.setType(type.name());
            return this;
        }

        public NewMapForm get() {
            return form;
        }

    }

}
