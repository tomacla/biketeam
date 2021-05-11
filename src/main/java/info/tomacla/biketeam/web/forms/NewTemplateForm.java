package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.global.RideGroupTemplate;
import info.tomacla.biketeam.domain.global.RideTemplate;
import info.tomacla.biketeam.domain.ride.RideType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NewTemplateForm {

    private String id;
    private String name;
    private String type;
    private String description;
    private String groups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type == null ? RideType.REGULAR.name() : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroups() {
        return groups == null ? "[]" : groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public static NewTemplateForm empty() {
        NewTemplateForm form = new NewTemplateForm();
        form.setId("new");
        form.setName("");
        form.setGroups(Json.serialize(List.of(new NewTemplateForm.NewRideGroupTemplateForm())));
        return form;
    }

    public static NewTemplateForm build(RideTemplate rideTemplate) {
        NewTemplateForm form = new NewTemplateForm();
        form.setId(rideTemplate.getId());
        form.setName(rideTemplate.getName());
        form.setType(rideTemplate.getType().name());
        form.setDescription(rideTemplate.getDescription());
        form.setGroups(Json.serialize(rideTemplate.getGroups().stream().map(NewRideGroupTemplateForm::build).collect(Collectors.toList())).replace("\\", "\\\\"));
        return form;
    }

    public static class NewRideGroupTemplateForm {

        private String id;
        private String name;
        private double lowerSpeed;
        private double upperSpeed;
        private String meetingLocation;
        private String meetingPoint;
        private String meetingTime;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getMeetingLocation() {
            return meetingLocation == null ? "" : meetingLocation;
        }

        public void setMeetingLocation(String meetingLocation) {
            this.meetingLocation = meetingLocation;
        }

        public String getMeetingPoint() {
            return meetingPoint;
        }

        public void setMeetingPoint(String meetingPoint) {
            this.meetingPoint = meetingPoint;
        }

        public String getMeetingTime() {
            return meetingTime == null ? "12:00" : meetingTime;
        }

        public void setMeetingTime(String meetingTime) {
            this.meetingTime = meetingTime;
        }

        public static NewRideGroupTemplateForm build(RideGroupTemplate groupTemplate) {
            NewRideGroupTemplateForm form = new NewRideGroupTemplateForm();
            form.setId(groupTemplate.getId());
            form.setLowerSpeed(groupTemplate.getLowerSpeed());
            form.setUpperSpeed(groupTemplate.getUpperSpeed());
            form.setName(groupTemplate.getName());
            form.setMeetingTime(groupTemplate.getMeetingTime().format(DateTimeFormatter.ISO_TIME));
            form.setMeetingPoint(Json.serialize(groupTemplate.getMeetingPoint()));
            form.setMeetingLocation(groupTemplate.getMeetingLocation());
            return form;
        }

    }

}
