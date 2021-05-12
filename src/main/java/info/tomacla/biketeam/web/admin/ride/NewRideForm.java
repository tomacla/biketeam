package info.tomacla.biketeam.web.admin.ride;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.global.RideGroupTemplate;
import info.tomacla.biketeam.domain.global.RideTemplate;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideType;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    private List<NewRideGroupForm> groups;

    public NewRideForm() {
        this(1);
    }

    public NewRideForm(int numberOfGroups) {
        setId(null);
        setType(null);
        setDate(null);
        setPublishedAtDate(null);
        setPublishedAtTime(null);
        setTitle(null);
        setFile(null);
        setDescription(null);
        groups = new ArrayList<>();
        for (int i = 0; i < (Math.max(numberOfGroups, 1)); i++) {
            groups.add(NewRideGroupForm.builder().withName("G" + (i + 1)).get());
        }
    }

    public NewRideForm(RideTemplate rideTemplate) {
        setId(null);
        setType(rideTemplate.getType().name());
        setDate(null);
        setPublishedAtDate(null);
        setPublishedAtTime(null);
        setTitle(rideTemplate.getName());
        setFile(null);
        setDescription(rideTemplate.getDescription());
        groups = new ArrayList<>();
        for (RideGroupTemplate g : rideTemplate.getGroups()) {
            groups.add(NewRideGroupForm.builder()
                    .withName(g.getName())
                    .withLowerSpeed(g.getLowerSpeed())
                    .withUpperSpeed(g.getUpperSpeed())
                    .withMeetingLocation(g.getMeetingLocation())
                    .withMeetingPoint(g.getMeetingPoint())
                    .withMeetingTime(g.getMeetingTime())
                    .get());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Strings.requireNonBlankOrDefault(type, RideType.REGULAR.name());
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = Strings.requireNonBlankOrDefault(date, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public String getPublishedAtDate() {
        return publishedAtDate;
    }

    public void setPublishedAtDate(String publishedAtDate) {
        this.publishedAtDate = Strings.requireNonBlankOrDefault(publishedAtDate, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public String getPublishedAtTime() {
        return publishedAtTime;
    }

    public void setPublishedAtTime(String publishedAtTime) {
        this.publishedAtTime = Strings.requireNonBlankOrDefault(publishedAtTime, LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME));
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean fileSet() {
        return file != null && !file.isEmpty();
    }

    public List<NewRideGroupForm> getGroups() {
        return groups;
    }

    public void setGroups(List<NewRideGroupForm> groups) {
        this.groups = Objects.requireNonNullElse(groups, new ArrayList<>());
    }

    public static NewRideFormBuilder builder(int numberOfGroups) {
        return new NewRideFormBuilder(numberOfGroups);
    }

    public static NewRideFormBuilder builder(RideTemplate template) {
        return new NewRideFormBuilder(template);
    }

    public NewRideFormParser parser() {
        return new NewRideFormParser(this);
    }

    public static class NewRideFormParser {

        private final NewRideForm form;

        public NewRideFormParser(NewRideForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getTitle() {
            return form.getTitle();
        }

        public LocalDate getDate() {
            return LocalDate.parse(form.getDate());
        }

        public ZonedDateTime getPublishedAt(ZoneId timezone) {
            return ZonedDateTime.of(LocalDateTime.parse(form.getPublishedAtDate() + "T" + form.getPublishedAtTime()), timezone);
        }

        public RideType getType() {
            return RideType.valueOf(form.getType());
        }

        public String getDescription() {
            return form.getDescription();
        }

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
        }

        public Set<RideGroup> getGroups(Ride parent, MapRepository mapRepository) {
            return form.getGroups().stream().map(g -> {
                NewRideGroupForm.NewRideGroupFormParser parser = g.parser();
                String mapId = null;
                if (parser.getMapId().isPresent()) {
                    mapId = mapRepository.findById(parser.getMapId().get()).isPresent() ? parser.getMapId().get() : null;
                }
                RideGroup gg = new RideGroup(parser.getName(),
                        parser.getLowerSpeed(),
                        parser.getUpperSpeed(),
                        mapId,
                        parser.getMeetingLocation(),
                        parser.getMeetingTime(),
                        parser.getMeetingPoint().orElse(null),
                        new HashSet<>());
                gg.setRide(parent);
                if (!parser.getId().equals("new")) {
                    gg.setId(parser.getId());
                }
                return gg;
            }).collect(Collectors.toSet());
        }

    }

    public static class NewRideFormBuilder {

        private final NewRideForm form;

        public NewRideFormBuilder(int numberOfGroups) {
            this.form = new NewRideForm(numberOfGroups);
        }

        public NewRideFormBuilder(RideTemplate template) {
            this.form = new NewRideForm(template);
        }

        public NewRideFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewRideFormBuilder withTitle(String title) {
            form.setTitle(title);
            return this;
        }

        public NewRideFormBuilder withType(RideType type) {
            form.setType(type.name());
            return this;
        }

        public NewRideFormBuilder withDescription(String description) {
            form.setDescription(description);
            return this;
        }

        public NewRideFormBuilder withDate(LocalDate date) {
            form.setDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return this;
        }

        public NewRideFormBuilder withPublishedAt(ZonedDateTime publishedAt) {
            form.setPublishedAtDate(publishedAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            form.setPublishedAtTime(publishedAt.truncatedTo(ChronoUnit.SECONDS).toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
            return this;
        }

        public NewRideFormBuilder withGroups(Set<RideGroup> groups) {
            if (groups != null) {
                form.setGroups(groups.stream().map(g -> NewRideGroupForm.builder()
                        .withId(g.getId())
                        .withName(g.getName())
                        .withMapId(g.getMapId())
                        .withLowerSpeed(g.getLowerSpeed())
                        .withUpperSpeed(g.getUpperSpeed())
                        .withMeetingLocation(g.getMeetingLocation())
                        .withMeetingTime(g.getMeetingTime())
                        .withMeetingPoint(g.getMeetingPoint())
                        .get()).collect(Collectors.toList()));
            }
            return this;
        }

        public NewRideForm get() {
            return form;
        }
    }


}
