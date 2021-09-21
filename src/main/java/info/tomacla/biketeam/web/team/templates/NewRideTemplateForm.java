package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NewRideTemplateForm {

    private String id;
    private String name;
    private String type;
    private String description;
    private List<NewRideGroupTemplateForm> groups;

    public NewRideTemplateForm() {
        this(1);
    }

    public NewRideTemplateForm(int numberOfGroups) {
        setId(null);
        setName(null);
        setType(null);
        setDescription(null);
        groups = new ArrayList<>();
        for (int i = 0; i < (Math.max(numberOfGroups, 1)); i++) {
            groups.add(NewRideGroupTemplateForm.builder().withName("G" + (i + 1)).get());
        }
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
        this.type = Strings.requireNonBlankOrDefault(type, RideType.REGULAR.name());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlankOrDefault(description, "");
    }

    public List<NewRideGroupTemplateForm> getGroups() {
        return groups;
    }

    public void setGroups(List<NewRideGroupTemplateForm> groups) {
        this.groups = Objects.requireNonNullElse(groups, new ArrayList<>());
    }

    public static NewRideTemplateFormBuilder builder(int numberOfGroups) {
        return new NewRideTemplateFormBuilder(numberOfGroups);
    }

    public NewRideTemplateFormParser parser() {
        return new NewRideTemplateFormParser(this);
    }

    public static class NewRideTemplateFormParser {

        private final NewRideTemplateForm form;

        public NewRideTemplateFormParser(NewRideTemplateForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getName() {
            return form.getName();
        }

        public RideType getType() {
            return RideType.valueOf(form.getType());
        }

        public String getDescription() {
            return form.getDescription();
        }

        public Set<RideGroupTemplate> getGroups() {
            return form.getGroups().stream().map(g -> {
                NewRideGroupTemplateForm.NewRideGroupTemplateFormParser parser = g.parser();
                RideGroupTemplate gg = new RideGroupTemplate(parser.getName(),
                        parser.getLowerSpeed(),
                        parser.getUpperSpeed(),
                        parser.getMeetingLocation(),
                        parser.getMeetingTime(),
                        parser.getMeetingPoint().orElse(null));
                return gg;
            }).collect(Collectors.toSet());
        }

    }

    public static class NewRideTemplateFormBuilder {

        private final NewRideTemplateForm form;

        public NewRideTemplateFormBuilder(int numberOfGroups) {
            this.form = new NewRideTemplateForm(numberOfGroups);
        }

        public NewRideTemplateFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewRideTemplateFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewRideTemplateFormBuilder withType(RideType type) {
            form.setType(type.name());
            return this;
        }

        public NewRideTemplateFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public NewRideTemplateFormBuilder withGroups(List<RideGroupTemplate> groups) {
            if (groups != null) {
                form.setGroups(groups.stream().map(g -> NewRideGroupTemplateForm.builder()
                        .withId(g.getId())
                        .withName(g.getName())
                        .withLowerSpeed(g.getLowerSpeed())
                        .withUpperSpeed(g.getUpperSpeed())
                        .withMeetingLocation(g.getMeetingLocation())
                        .withMeetingTime(g.getMeetingTime())
                        .withMeetingPoint(g.getMeetingPoint())
                        .get()).collect(Collectors.toList()));
            }
            return this;
        }

        public NewRideTemplateForm get() {
            return form;
        }
    }

}
