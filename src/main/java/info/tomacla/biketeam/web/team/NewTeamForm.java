package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.common.Timezone;

import java.util.Objects;

public class NewTeamForm {

    private String id;
    private String name;
    private String city;
    private String country;
    private String description;
    private String timezone;

    public NewTeamForm() {
        setId(null);
        setCity(null);
        setCountry(null);
        setName(null);
        setTimezone(null);
        setDescription(null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNullElse(id, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = Objects.requireNonNullElse(city, "Paris");
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = Objects.requireNonNullElse(country, "FR");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = Objects.requireNonNullElse(timezone, Timezone.DEFAULT_TIMEZONE);
    }

    public static NewTeamFormBuilder builder() {
        return new NewTeamFormBuilder();
    }

    public NewTeamFormParser parser() {
        return new NewTeamFormParser(this);
    }

    public static class NewTeamFormParser {

        private final NewTeamForm form;

        public NewTeamFormParser(NewTeamForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getName() {
            return form.getName();
        }

        public String getCity() {
            return form.getCity();
        }

        public String getCountry() {
            return form.getCountry();
        }

        public String getDescription() {
            return form.getDescription();
        }

        public String getTimezone() {
            return form.getTimezone();
        }

    }

    public static class NewTeamFormBuilder {

        private final NewTeamForm form;

        public NewTeamFormBuilder() {
            this.form = new NewTeamForm();
        }

        public NewTeamFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewTeamFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewTeamFormBuilder withCity(String city) {
            form.setCity(city);
            return this;
        }

        public NewTeamFormBuilder withCountry(String country) {
            form.setCountry(country);
            return this;
        }

        public NewTeamFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public NewTeamFormBuilder withTimezone(String timezone) {
            form.setTimezone(timezone);
            return this;
        }

        public NewTeamForm get() {
            return form;
        }

    }


}
