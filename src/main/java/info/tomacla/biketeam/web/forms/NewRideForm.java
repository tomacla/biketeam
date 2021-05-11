package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.global.RideGroupTemplate;
import info.tomacla.biketeam.domain.global.RideTemplate;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideType;
import org.apache.tomcat.jni.Local;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NewRideForm {

    private String id;
    private String type;
    private String date;
    private String publishedAtDate;
    private String publishedAtTime;
    private String title;
    private String description;
    private MultipartFile file;
    private String groups;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type == null ? RideType.REGULAR.name() : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date == null ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPublishedAtDate() {
        return publishedAtDate;
    }

    public void setPublishedAtDate(String publishedAtDate) {
        this.publishedAtDate = publishedAtDate;
    }

    public String getPublishedAtTime() {
        return publishedAtTime;
    }

    public void setPublishedAtTime(String publishedAtTime) {
        this.publishedAtTime = publishedAtTime;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getGroups() {
        return groups == null ? "[]" : groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public static NewRideForm empty() {
        NewRideForm form = new NewRideForm();
        form.setId("new");
        form.setPublishedAtDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setPublishedAtTime(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        form.setGroups(Json.serialize(List.of(new NewRideForm.NewRideGroupForm())));
        return form;
    }

    public static NewRideForm build(Ride ride) {
        NewRideForm form = new NewRideForm();
        form.setId(ride.getId());
        form.setType(ride.getType().name());
        form.setDate(ride.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.publishedAtDate = ride.getPublishedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        form.publishedAtTime = ride.getPublishedAt().toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
        form.setDescription(ride.getDescription());
        form.setTitle(ride.getTitle());
        form.setGroups(Json.serialize(ride.getGroups().stream().map(NewRideGroupForm::build).collect(Collectors.toList())).replace("\\", "\\\\"));
        return form;
    }

    public static Object fromTemplate(RideTemplate rideTemplate) {
        NewRideForm form = new NewRideForm();
        form.setId("new");
        form.setPublishedAtDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setPublishedAtTime(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        form.setType(rideTemplate.getType().name());
        form.setDescription(rideTemplate.getDescription());
        form.setTitle(rideTemplate.getName());
        form.setGroups(Json.serialize(rideTemplate.getGroups().stream().map(NewRideGroupForm::fromTemplate).collect(Collectors.toList())).replace("\\", "\\\\"));
        return form;
    }

    public static class NewRideGroupForm {

        private String id;
        private String name;
        private double lowerSpeed;
        private double upperSpeed;
        private String mapId;
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

        public String getMapId() {
            return mapId == null ? "" : mapId;
        }

        public void setMapId(String mapId) {
            this.mapId = mapId;
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

        public static NewRideGroupForm build(RideGroup group) {
            NewRideGroupForm form = new NewRideGroupForm();
            form.setId(group.getId());
            form.setLowerSpeed(group.getLowerSpeed());
            form.setUpperSpeed(group.getUpperSpeed());
            form.setMapId(group.getMapId());
            form.setName(group.getName());
            form.setMeetingTime(group.getMeetingTime().format(DateTimeFormatter.ISO_TIME));
            form.setMeetingPoint(Json.serialize(group.getMeetingPoint()));
            form.setMeetingLocation(group.getMeetingLocation());
            return form;
        }

        public static NewRideGroupForm fromTemplate(RideGroupTemplate groupTemplate) {
            NewRideGroupForm form = new NewRideGroupForm();
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
