package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;

import java.time.LocalTime;

public class NewRideGroupForm {

    private String id = "";
    private String name = "";
    private double lowerSpeed = 28;
    private double upperSpeed = 30;
    private String mapId = "";
    private String mapName = "";
    private String meetingTime = "12:00";

    public static NewRideGroupFormBuilder builder() {
        return new NewRideGroupFormBuilder();
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

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = Strings.requireNonBlankOrDefault(meetingTime, "12:00");
    }

    public NewRideGroupFormParser parser() {
        return new NewRideGroupFormParser(this);
    }

    public static class NewRideGroupFormParser {

        private final NewRideGroupForm form;

        public NewRideGroupFormParser(NewRideGroupForm form) {
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

        public String getMapId() {
            return Strings.requireNonBlankOrNull(form.getMapId());
        }

    }

    public static class NewRideGroupFormBuilder {

        private final NewRideGroupForm form;

        public NewRideGroupFormBuilder() {
            this.form = new NewRideGroupForm();
        }

        public NewRideGroupFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewRideGroupFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public NewRideGroupFormBuilder withLowerSpeed(double lowerSpeed) {
            form.setLowerSpeed(lowerSpeed);
            return this;
        }

        public NewRideGroupFormBuilder withUpperSpeed(double upperSpeed) {
            form.setUpperSpeed(upperSpeed);
            return this;
        }

        public NewRideGroupFormBuilder withMeetingTime(LocalTime meetingTime) {
            if (meetingTime != null) {
                form.setMeetingTime(Dates.formatTime(meetingTime));
            }
            return this;
        }

        public NewRideGroupFormBuilder withMapId(String mapId) {
            form.setMapId(mapId);
            return this;
        }

        public NewRideGroupFormBuilder withMapName(String mapName) {
            form.setMapName(mapName);
            return this;
        }

        public NewRideGroupForm get() {
            return form;
        }

    }

}