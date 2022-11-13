package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;

import java.time.LocalTime;

public class NewRideGroupTemplateForm {

    private String id = "";
    private String name = "";
    private double averageSpeed = 30;
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

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
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


        public double getAverageSpeed() {
            return form.getAverageSpeed();
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


        public NewRideGroupTemplateFormBuilder withAverageSpeed(double averageSpeed) {
            form.setAverageSpeed(averageSpeed);
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