package info.tomacla.biketeam.web.team.map;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.MapType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NewMapForm {

    private String id = "new";
    private String permalink = "";
    private String name = "";
    private String type = MapType.ROAD.name();
    private String visible = "on";
    private List<String> tags = new ArrayList<>();
    private MultipartFile file = null;

    public static NewMapFormBuilder builder() {
        return new NewMapFormBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = Strings.requireNonBlankOrDefault(permalink, "");
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

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean fileSet() {
        return file != null && !file.isEmpty();
    }

    public NewMapFormParser parser() {
        return new NewMapFormParser(this);
    }

    public static class NewMapFormParser {

        private final NewMapForm form;

        public NewMapFormParser(NewMapForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getPermalink() {
            if (Strings.isBlank(form.getPermalink())) {
                return null;
            }
            return Strings.normalizePermalink(form.getPermalink());
        }

        public String getName() {
            return Strings.requireNonBlankOrNull(form.getName());
        }

        public boolean isVisible() {
            return form.getVisible() != null && form.getVisible().equals("on");
        }

        public List<String> getTags() {
            return form.getTags();
        }

        public MapType getType() {
            return MapType.valueOf(form.getType());
        }

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
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

        public NewMapFormBuilder withPermalink(String permalink) {
            form.setPermalink(permalink);
            return this;
        }

        public NewMapFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewMapFormBuilder withVisible(boolean visible) {
            form.setVisible(visible ? "on" : null);
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
