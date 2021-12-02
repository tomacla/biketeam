package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;

import java.time.LocalTime;

public class NewRideGroupForm {

    private String id = "";
    private String name = "";
    private double lowerSpeed = 28;
    private double upperSpeed = 30;
    private String mapId = "";
    private String mapName = "";
    private String meetingLocation = "";
    private double meetingPointLat = 0.0;
    private double meetingPointLng = 0.0;
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

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = Strings.requireNonBlankOrDefault(meetingLocation, "");
    }

    public double getMeetingPointLat() {
        return meetingPointLat;
    }

    public void setMeetingPointLat(double meetingPointLat) {
        this.meetingPointLat = meetingPointLat;
    }

    public double getMeetingPointLng() {
        return meetingPointLng;
    }

    public void setMeetingPointLng(double meetingPointLng) {
        this.meetingPointLng = meetingPointLng;
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

        public String getMeetingLocation() {
            return Strings.requireNonBlankOrNull(form.getMeetingLocation());
        }

        public Point getMeetingPoint() {
            if (form.getMeetingPointLat() != 0.0 && form.getMeetingPointLng() != 0.0) {
                return new Point(form.getMeetingPointLat(), form.getMeetingPointLng());
            }
            return null;
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

        public NewRideGroupFormBuilder withMeetingLocation(String meetingLocation) {
            form.setMeetingLocation(meetingLocation);
            return this;
        }

        public NewRideGroupFormBuilder withMeetingPoint(Point meetingPoint) {
            if (meetingPoint != null) {
                form.setMeetingPointLat(meetingPoint.getLat());
                form.setMeetingPointLng(meetingPoint.getLng());
            }
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