package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.trip.TripStage;
import info.tomacla.biketeam.service.MapService;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class NewTripForm {

    private String id = "new";
    private String permalink = "";
    private String type = MapType.ROAD.name();
    private String startDate = "";
    private String endDate = "";
    private String publishedAtDate = "";
    private String publishedAtTime = "";
    private String title = "";
    private String description = "";
    private double lowerSpeed = 28;
    private double upperSpeed = 30;
    private String meetingLocation = "";
    private double meetingPointLat = 0.0;
    private double meetingPointLng = 0.0;
    private String meetingTime = "08:00";
    private MultipartFile file;
    private List<NewTripStageForm> stages = new ArrayList<>();

    public NewTripForm() {
        this(ZonedDateTime.now(ZoneOffset.UTC));
    }

    public NewTripForm(ZonedDateTime defaultDate) {
        stages.add(NewTripStageForm.builder().withName("Aller").withDate(defaultDate.toLocalDate()).get());
        stages.add(NewTripStageForm.builder().withName("Retour").withDate(defaultDate.plus(1, ChronoUnit.DAYS).toLocalDate()).get());
    }

    public static NewTripFormBuilder builder(ZonedDateTime defaultDate, ZoneId timezone) {
        return new NewTripFormBuilder(defaultDate, timezone);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = Strings.requireNonBlankOrDefault(permalink, "");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Strings.requireNonBlankOrDefault(type, MapType.ROAD.name());
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = Strings.requireNonBlankOrDefault(startDate, "");
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = Strings.requireNonBlankOrDefault(endDate, "");
    }

    public String getPublishedAtDate() {
        return publishedAtDate;
    }

    public void setPublishedAtDate(String publishedAtDate) {
        this.publishedAtDate = Strings.requireNonBlankOrDefault(publishedAtDate, Dates.formatDate(LocalDate.now()));
    }

    public String getPublishedAtTime() {
        return publishedAtTime;
    }

    public void setPublishedAtTime(String publishedAtTime) {
        this.publishedAtTime = Strings.requireNonBlankOrDefault(publishedAtTime, "12:00");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Strings.requireNonBlankOrDefault(title, "");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Strings.requireNonBlankOrDefault(description, "");
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean fileSet() {
        return file != null && !file.isEmpty();
    }

    public List<NewTripStageForm> getStages() {
        return stages;
    }

    public void setStages(List<NewTripStageForm> stages) {
        this.stages = Objects.requireNonNullElse(stages, new ArrayList<>());
    }

    public NewTripFormParser parser() {
        return new NewTripFormParser(this);
    }

    public static class NewTripFormParser {

        private final NewTripForm form;

        public NewTripFormParser(NewTripForm form) {
            this.form = form;
        }

        public String getId() {
            return Strings.requireNonBlankOrNull(form.getId());
        }

        public String getPermalink() {
            return Strings.requireNonBlankOrNull(form.getPermalink());
        }

        public String getTitle() {
            return Strings.requireNonBlankOrNull(form.getTitle());
        }

        public LocalDate getStartDate() {
            return LocalDate.parse(form.getStartDate());
        }

        public LocalDate getEndDate() {
            return LocalDate.parse(form.getEndDate());
        }

        public ZonedDateTime getPublishedAt(ZoneId timezone) {
            return Dates.parseZonedDateTimeInUTC(form.getPublishedAtDate(), form.getPublishedAtTime(), timezone);
        }

        public MapType getType() {
            return MapType.valueOf(form.getType());
        }

        public String getDescription() {
            return Strings.requireNonBlankOrNull(form.getDescription());
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

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
        }

        public List<TripStage> getStages(String teamId, MapService mapService) {
            return form.getStages().stream().map(st -> {
                NewTripStageForm.NewTripStageFormParser parser = st.parser();
                Map map = null;
                if (parser.getMapId().isPresent()) {
                    map = mapService.get(teamId, parser.getMapId().get()).orElse(null);
                }

                final TripStage ss = new TripStage();
                ss.setName(parser.getName());
                ss.setDate(parser.getDate());
                ss.setMap(map);

                if (parser.getId() != null) {
                    ss.setId(parser.getId());
                }

                return ss;

            }).collect(Collectors.toList());
        }

    }

    public static class NewTripFormBuilder {

        private final NewTripForm form;

        public NewTripFormBuilder(ZonedDateTime defaultDate, ZoneId timezone) {
            this.form = new NewTripForm(defaultDate);
            withPublishedAt(defaultDate, timezone);
            withStartDate(defaultDate.toLocalDate());
            withEndDate(defaultDate.plus(1, ChronoUnit.DAYS).toLocalDate());
        }

        public NewTripFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewTripFormBuilder withPermalink(String permalink) {
            form.setPermalink(permalink);
            return this;
        }

        public NewTripFormBuilder withTitle(String title) {
            form.setTitle(title);
            return this;
        }

        public NewTripFormBuilder withType(MapType type) {
            form.setType(type.name());
            return this;
        }

        public NewTripFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public NewTripFormBuilder withStartDate(LocalDate startDate) {
            form.setStartDate(Dates.formatDate(startDate));
            return this;
        }

        public NewTripFormBuilder withEndDate(LocalDate endDate) {
            form.setEndDate(Dates.formatDate(endDate));
            return this;
        }

        public NewTripFormBuilder withPublishedAt(ZonedDateTime publishedAt, ZoneId timezone) {
            form.setPublishedAtDate(Dates.formatZonedDateInTimezone(publishedAt, timezone));
            form.setPublishedAtTime(Dates.formatZonedTimeInTimezone(publishedAt, timezone));
            return this;
        }

        public NewTripFormBuilder withLowerSpeed(double lowerSpeed) {
            form.setLowerSpeed(lowerSpeed);
            return this;
        }

        public NewTripFormBuilder withUpperSpeed(double upperSpeed) {
            form.setUpperSpeed(upperSpeed);
            return this;
        }

        public NewTripFormBuilder withMeetingLocation(String meetingLocation) {
            form.setMeetingLocation(meetingLocation);
            return this;
        }

        public NewTripFormBuilder withMeetingPoint(Point meetingPoint) {
            if (meetingPoint != null) {
                form.setMeetingPointLat(meetingPoint.getLat());
                form.setMeetingPointLng(meetingPoint.getLng());
            }
            return this;
        }

        public NewTripFormBuilder withMeetingTime(LocalTime meetingTime) {
            if (meetingTime != null) {
                form.setMeetingTime(Dates.formatTime(meetingTime));
            }
            return this;
        }

        public NewTripFormBuilder withStages(List<TripStage> stages) {
            if (stages != null) {
                form.setStages(stages.stream().map(st -> {

                    String mapId = null;
                    String mapName = null;
                    if (st.getMap() != null) {
                        mapId = st.getMap().getId();
                        mapName = st.getMap().getName();
                    }

                    return NewTripStageForm.builder()
                            .withId(st.getId())
                            .withName(st.getName())
                            .withDate(st.getDate())
                            .withMapId(mapId)
                            .withMapName(mapName)
                            .get();
                }).collect(Collectors.toList()));
            }
            return this;
        }

        public NewTripForm get() {
            return form;
        }
    }


}