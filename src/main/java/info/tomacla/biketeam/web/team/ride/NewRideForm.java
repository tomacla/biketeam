package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.PlaceService;
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

    private String id = "new";
    private String permalink = "";
    private String type = RideType.REGULAR.name();
    private String date = "";
    private String publishedAtDate = Dates.formatDate(LocalDate.now());
    private String publishedAtTime = "12:00";
    private String title = "";
    private String description = "";
    private String templateId = "";
    private String startPlaceId = "";
    private String endPlaceId = "";
    private String listedInFeed = null;
    private MultipartFile file = null;
    private List<NewRideGroupForm> groups = new ArrayList<>();

    public NewRideForm() {
        this(1);
    }

    public NewRideForm(int numberOfGroups) {
        for (int i = 0; i < (Math.max(numberOfGroups, 1)); i++) {
            groups.add(NewRideGroupForm.builder().withName("G" + (i + 1)).get());
        }
    }

    public NewRideForm(RideTemplate rideTemplate) {
        setType(rideTemplate.getType().name());
        if (rideTemplate.getIncrement() != null) {
            setTitle(rideTemplate.getName() + " #" + rideTemplate.getIncrement());
        } else {
            setTitle(rideTemplate.getName());
        }
        setDescription(rideTemplate.getDescription());
        setTemplateId(rideTemplate.getId());
        groups = new ArrayList<>();
        for (RideGroupTemplate g : rideTemplate.getSortedGroups()) {
            groups.add(NewRideGroupForm.builder()
                    .withName(g.getName())
                    .withAverageSpeed(g.getAverageSpeed())
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

    public String getListedInFeed() {
        return listedInFeed;
    }

    public void setListedInFeed(String listedInFeed) {
        this.listedInFeed = listedInFeed;
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

    public String getStartPlaceId() {
        return startPlaceId;
    }

    public void setStartPlaceId(String startPlaceId) {
        this.startPlaceId = Strings.requireNonBlankOrDefault(startPlaceId, "");
        ;
    }

    public String getEndPlaceId() {
        return endPlaceId;
    }

    public void setEndPlaceId(String endPlaceId) {
        this.endPlaceId = Strings.requireNonBlankOrDefault(endPlaceId, "");
        ;
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
            return Strings.requireNonBlankOrNull(form.getId());
        }

        public String getPermalink() {
            return Strings.requireNonBlankOrNull(form.getPermalink());
        }

        public String getTitle() {
            return Strings.requireNonBlankOrNull(form.getTitle());
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
            return Strings.requireNonBlankOrNull(form.getDescription());
        }

        public String getTemplateId() {
            return Strings.requireNonBlankOrNull(form.getTemplateId());
        }

        public Place getStartPlace(String teamId, PlaceService placeService) {
            if (!form.getStartPlaceId().equals("")) {
                return placeService.get(teamId, form.getStartPlaceId()).orElse(null);
            }
            return null;
        }

        public Place getEndPlace(String teamId, PlaceService placeService) {
            if (!form.getEndPlaceId().equals("")) {
                return placeService.get(teamId, form.getEndPlaceId()).orElse(null);
            }
            return null;
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
                if (parser.getMapId() != null) {
                    map = mapService.get(teamId, parser.getMapId()).orElse(null);
                }
                final RideGroup gg = new RideGroup();
                gg.setName(parser.getName());
                gg.setAverageSpeed(parser.getAverageSpeed());
                gg.setMap(map);
                gg.setMeetingTime(parser.getMeetingTime());

                if (parser.getId() != null) {
                    gg.setId(parser.getId());
                }

                return gg;

            }).collect(Collectors.toList());
        }

        public boolean isListedInFeed() {
            return form.getListedInFeed() != null && form.getListedInFeed().equals("on");
        }

    }

    public static class NewRideFormBuilder {

        private final NewRideForm form;

        public NewRideFormBuilder(int numberOfGroups, ZonedDateTime defaultDate, ZoneId timezone) {
            this.form = new NewRideForm(numberOfGroups);
            withPublishedAt(defaultDate, timezone);
            withDate(defaultDate.toLocalDate());
            withListedInFeed(true);
        }

        public NewRideFormBuilder(RideTemplate template, ZonedDateTime defaultDate, ZoneId timezone) {
            this.form = new NewRideForm(template);
            withPublishedAt(defaultDate, timezone);
            withDate(defaultDate.toLocalDate());
            withStartPlace(template.getStartPlace());
            withEndPlace(template.getEndPlace());
            withListedInFeed(true);
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

        public NewRideFormBuilder withStartPlace(Place startPlace) {
            if (startPlace != null) {
                form.setStartPlaceId(startPlace.getId());
            }
            return this;
        }

        public NewRideFormBuilder withEndPlace(Place endPlace) {
            if (endPlace != null) {
                form.setEndPlaceId(endPlace.getId());
            }
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
                            .withAverageSpeed(g.getAverageSpeed())
                            .withMeetingTime(g.getMeetingTime())
                            .get();
                }).collect(Collectors.toList()));
            }
            return this;
        }

        public NewRideFormBuilder withListedInFeed(boolean listedInFeed) {
            form.setListedInFeed(listedInFeed ? "on" : null);
            return this;
        }

        public NewRideForm get() {
            return form;
        }
    }


}
