package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;

import java.time.LocalTime;

public class NewRideGroupTemplateForm {

    private String id = "";
    private String name = "";
    private double lowerSpeed = 28;
    private double upperSpeed = 30;
    private String meetingTime = "12:00";

    public static NewRideGroupTemplateFormBuilder builder() {
        return new NewRideGroupTemplateFormBuilder();
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

    public double getLowerSpeed() {
        return lowerSpeed;
    }

    public void setLowerSpeed(double lowerSpeed) {
        this.lowerSpeed = lowerSpeed;
    }

    public double getUpperSpeed() {
        return upperSpeed;
    }

    public void setUpperSpeed(double upperSpeed) {
        this.upperSpeed = upperSpeed;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = Strings.requireNonBlankOrDefault(meetingTime, "12:00");
    }

    public NewRideGroupTemplateFormParser parser() {
        return new NewRideGroupTemplateFormParser(this);
    }

    public static class NewRideGroupTemplateFormParser {

        private final NewRideGroupTemplateForm form;

        public NewRideGroupTemplateFormParser(NewRideGroupTemplateForm form) {
            this.form = form;
        }

        public String getId() {
            return Strings.requireNonBlankOrNull(form.getId());
        }

        public String getName() {
            return Strings.requireNonBlankOrNull(form.getName());
        }

        public double getLowerSpeed() {
            return form.getLowerSpeed();
        }

        public double getUpperSpeed() {
            return form.getUpperSpeed();
        }

        public LocalTime getMeetingTime() {
            return LocalTime.parse(form.getMeetingTime());
        }

    }

    public static class NewRideGroupTemplateFormBuilder {

        private final NewRideGroupTemplateForm form;

        public NewRideGroupTemplateFormBuilder() {
            this.form = new NewRideGroupTemplateForm();
        }

        public NewRideGroupTemplateFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewRideGroupTemplateFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewRideGroupTemplateFormBuilder withLowerSpeed(double lowerSpeed) {
            form.setLowerSpeed(lowerSpeed);
            return this;
        }

        public NewRideGroupTemplateFormBuilder withUpperSpeed(double upperSpeed) {
            form.setUpperSpeed(upperSpeed);
            return this;
        }

        public NewRideGroupTemplateFormBuilder withMeetingTime(LocalTime meetingTime) {
            if (meetingTime != null) {
                form.setMeetingTime(Dates.formatTime(meetingTime));
            }
            return this;
        }

        public NewRideGroupTemplateForm get() {
            return form;
        }

    }

}