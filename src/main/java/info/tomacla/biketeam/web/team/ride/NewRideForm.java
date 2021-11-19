package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.MapService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class NewRideForm {

    private String id;
    private String permalink;
    private String type;
    private String date;
    private String publishedAtDate;
    private String publishedAtTime;
    private String title;
    private String description;
    private String templateId;
    private MultipartFile file;
    private List<NewRideGroupForm> groups;

    public NewRideForm() {
        this(1);
    }

    public NewRideForm(int numberOfGroups) {
        setId(null);
        setPermalink(null);
        setType(null);
        setDate(null);
        setPublishedAtDate(null);
        setPublishedAtTime(null);
        setTitle(null);
        setFile(null);
        setDescription(null);
        setTemplateId(null);
        groups = new ArrayList<>();
        for (int i = 0; i < (Math.max(numberOfGroups, 1)); i++) {
            groups.add(NewRideGroupForm.builder().withName("G" + (i + 1)).get());
        }
    }

    public NewRideForm(RideTemplate rideTemplate) {
        setId(null);
        setPermalink(null);
        setType(rideTemplate.getType().name());
        setDate(null);
        setPublishedAtDate(null);
        setPublishedAtTime(null);
        if (rideTemplate.getIncrement() != null) {
            setTitle(rideTemplate.getName() + " #" + rideTemplate.getIncrement());
        } else {
            setTitle(rideTemplate.getName());
        }
        setFile(null);
        setDescription(rideTemplate.getDescription());
        setTemplateId(rideTemplate.getId());
        groups = new ArrayList<>();
        for (RideGroupTemplate g : rideTemplate.getSortedGroups()) {
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

    public static NewRideFormBuilder builder(int numberOfGroups, ZonedDateTime defaultDate, ZoneId timezone) {
        return new NewRideFormBuilder(numberOfGroups, defaultDate, timezone);
    }

    public static NewRideFormBuilder builder(RideTemplate template, ZonedDateTime defaultDate, ZoneId timezone) {
        return new NewRideFormBuilder(template, defaultDate, timezone);
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
        this.type = Strings.requireNonBlankOrDefault(type, RideType.REGULAR.name());
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = Strings.requireNonBlankOrDefault(date, "");
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = Strings.requireNonBlankOrDefault(templateId, "");
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

        public String getPermalink() {
            if (ObjectUtils.isEmpty(form.getPermalink())) {
                return null;
            }
            return form.getPermalink();
        }

        public String getTitle() {
            return form.getTitle();
        }

        public LocalDate getDate() {
            return LocalDate.parse(form.getDate());
        }

        public ZonedDateTime getPublishedAt(ZoneId timezone) {
            return Dates.parseZonedDateTimeInUTC(form.getPublishedAtDate(), form.getPublishedAtTime(), timezone);
        }

        public RideType getType() {
            return RideType.valueOf(form.getType());
        }

        public String getDescription() {
            return form.getDescription();
        }

        public String getTemplateId() {
            if (form.getTemplateId() == null || form.getTemplateId().isBlank()) {
                return null;
            }
            return form.getTemplateId();
        }

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
        }

        public List<RideGroup> getGroups(String teamId, MapService mapService) {
            return form.getGroups().stream().map(g -> {
                NewRideGroupForm.NewRideGroupFormParser parser = g.parser();
                Map map = null;
                if (parser.getMapId().isPresent()) {
                    map = mapService.get(teamId, parser.getMapId().get()).orElse(null);
                }
                final RideGroup gg = new RideGroup(parser.getName(),
                        parser.getLowerSpeed(),
                        parser.getUpperSpeed(),
                        map,
                        parser.getMeetingLocation(),
                        parser.getMeetingTime(),
                        parser.getMeetingPoint().orElse(null));

                if (parser.getId() != null) {
                    gg.setId(parser.getId());
                }

                return gg;

            }).collect(Collectors.toList());
        }

    }

    public static class NewRideFormBuilder {

        private final NewRideForm form;

        public NewRideFormBuilder(int numberOfGroups, ZonedDateTime defaultDate, ZoneId timezone) {
            this.form = new NewRideForm(numberOfGroups);
            withPublishedAt(defaultDate, timezone);
            withDate(defaultDate.toLocalDate());
        }

        public NewRideFormBuilder(RideTemplate template, ZonedDateTime defaultDate, ZoneId timezone) {
            this.form = new NewRideForm(template);
            withPublishedAt(defaultDate, timezone);
            withDate(defaultDate.toLocalDate());
        }

        public NewRideFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewRideFormBuilder withPermalink(String permalink) {
            form.setPermalink(permalink);
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
            form.setDate(Dates.formatDate(date));
            return this;
        }

        public NewRideFormBuilder withPublishedAt(ZonedDateTime publishedAt, ZoneId timezone) {
            form.setPublishedAtDate(Dates.formatZonedDateInTimezone(publishedAt, timezone));
            form.setPublishedAtTime(Dates.formatZonedTimeInTimezone(publishedAt, timezone));
            return this;
        }

        public NewRideFormBuilder withGroups(List<RideGroup> groups) {
            if (groups != null) {
                form.setGroups(groups.stream().map(g -> {

                    String mapId = null;
                    String mapName = null;
                    if (g.getMap() != null) {
                        mapId = g.getMap().getId();
                        mapName = g.getMap().getName();
                    }

                    return NewRideGroupForm.builder()
                            .withId(g.getId())
                            .withName(g.getName())
                            .withMapId(mapId)
                            .withMapName(mapName)
                            .withLowerSpeed(g.getLowerSpeed())
                            .withUpperSpeed(g.getUpperSpeed())
                            .withMeetingLocation(g.getMeetingLocation())
                            .withMeetingTime(g.getMeetingTime())
                            .withMeetingPoint(g.getMeetingPoint())
                            .get();
                }).collect(Collectors.toList()));
            }
            return this;
        }

        public NewRideForm get() {
            return form;
        }
    }


}
