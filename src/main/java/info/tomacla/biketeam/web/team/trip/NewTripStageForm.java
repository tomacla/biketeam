package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.common.Strings;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.Optional;

public class NewTripStageForm {

    private String id;
    private String name;
    private String mapId;
    private String mapName;
    private String date;

    public NewTripStageForm() {
        setId(null);
        setName(null);
        setMapId(null);
        setMapName(null);
        setDate(null);
    }

    public static NewTripStageFormBuilder builder() {
        return new NewTripStageFormBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlankOrDefault(name, "");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = Strings.requireNonBlankOrDefault(date, "");
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = Strings.requireNonBlankOrDefault(mapId, "");
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = Strings.requireNonBlankOrDefault(mapName, "");
    }

    public NewTripStageFormParser parser() {
        return new NewTripStageFormParser(this);
    }

    public static class NewTripStageFormParser {

        private final NewTripStageForm form;

        public NewTripStageFormParser(NewTripStageForm form) {
            this.form = form;
        }

        public String getId() {
            if (ObjectUtils.isEmpty(form.getId())) {
                return null;
            }
            return form.getId();
        }

        public String getName() {
            return form.getName();
        }

        public LocalDate getDate() {
            return LocalDate.parse(form.getDate());
        }

        public Optional<String> getMapId() {
            if (!form.getMapId().isBlank()) {
                return Optional.of(form.getMapId());
            }
            return Optional.empty();
        }

    }

    public static class NewTripStageFormBuilder {

        private final NewTripStageForm form;

        public NewTripStageFormBuilder() {
            this.form = new NewTripStageForm();
        }

        public NewTripStageFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewTripStageFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewTripStageFormBuilder withMapId(String mapId) {
            form.setMapId(mapId);
            return this;
        }

        public NewTripStageFormBuilder withMapName(String mapName) {
            form.setMapName(mapName);
            return this;
        }

        public NewTripStageFormBuilder withDate(LocalDate date) {
            form.setDate(Dates.formatDate(date));
            return this;
        }

        public NewTripStageForm get() {
            return form;
        }

    }

}