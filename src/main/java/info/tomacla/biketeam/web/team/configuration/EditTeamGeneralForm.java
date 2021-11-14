package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.domain.team.Visibility;

import java.util.Objects;

public class EditTeamGeneralForm {

    private String name;
    private String description;
    private String visibility;

    public EditTeamGeneralForm() {
        setName(null);
        setDescription(null);
        setVisibility(null);
    }

    public static EditTeamGeneralFormBuilder builder() {
        return new EditTeamGeneralFormBuilder();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = Objects.requireNonNullElse(visibility, Visibility.PUBLIC.name());
    }

    public EditTeamGeneralFormParser parser() {
        return new EditTeamGeneralFormParser(this);
    }

    public static class EditTeamGeneralFormParser {

        private final EditTeamGeneralForm form;

        protected EditTeamGeneralFormParser(EditTeamGeneralForm form) {
            this.form = form;
        }

        public String getDescription() {
            return form.getDescription();
        }

        public Visibility getVisibility() {
            return Visibility.valueOf(form.getVisibility());
        }

        public String getName() {
            return form.getName();
        }


    }

    public static class EditTeamGeneralFormBuilder {

        private final EditTeamGeneralForm form;


        protected EditTeamGeneralFormBuilder() {
            this.form = new EditTeamGeneralForm();
        }


        public EditTeamGeneralFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public EditTeamGeneralFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public EditTeamGeneralFormBuilder withVisibility(Visibility visibility) {
            form.setVisibility(visibility.name());
            return this;
        }

        public EditTeamGeneralForm get() {
            return form;
        }

    }

}
