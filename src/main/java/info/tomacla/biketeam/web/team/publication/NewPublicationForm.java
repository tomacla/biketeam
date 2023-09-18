package info.tomacla.biketeam.web.team.publication;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class NewPublicationForm {

    private String id = "new";
    private String title = "";
    private String content = "";
    private String publishedAtDate = Dates.formatDate(LocalDate.now());
    private String publishedAtTime = "12:00";
    private String allowRegistration = null;
    private MultipartFile file = null;

    public static NewPublicationFormBuilder builder(ZonedDateTime defaultPublishedAt, ZoneId timezone) {
        return new NewPublicationFormBuilder(defaultPublishedAt, timezone);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Strings.requireNonBlankOrDefault(title, "");
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = Strings.requireNonBlankOrDefault(content, "");
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

    public String getAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(String allowRegistration) {
        this.allowRegistration = allowRegistration;
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

    public NewPublicationFormParser parser() {
        return new NewPublicationFormParser(this);
    }

    public static class NewPublicationFormParser {

        private final NewPublicationForm form;

        public NewPublicationFormParser(NewPublicationForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getTitle() {
            return Strings.requireNonBlankOrNull(form.getTitle());
        }

        public String getContent() {
            return Strings.requireNonBlankOrNull(form.getContent());
        }

        public ZonedDateTime getPublishedAt(ZoneId timezone) {
            return Dates.parseZonedDateTimeInUTC(form.getPublishedAtDate(), form.getPublishedAtTime(), timezone);
        }

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
        }

        public boolean isAllowRegistration() {
            return form.getAllowRegistration() != null && form.getAllowRegistration().equals("on");
        }

    }

    public static class NewPublicationFormBuilder {

        private final NewPublicationForm form;

        public NewPublicationFormBuilder(ZonedDateTime defaultPublishedAt, ZoneId timezone) {
            this.form = new NewPublicationForm();
            withPublishedAt(defaultPublishedAt, timezone);
        }

        public NewPublicationFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewPublicationFormBuilder withTitle(String title) {
            form.setTitle(title);
            return this;
        }

        public NewPublicationFormBuilder withContent(String content) {
            form.setContent(content);
            return this;
        }

        public NewPublicationFormBuilder withPublishedAt(ZonedDateTime publishedAt, ZoneId timezone) {
            form.setPublishedAtDate(Dates.formatZonedDateInTimezone(publishedAt, timezone));
            form.setPublishedAtTime(Dates.formatZonedTimeInTimezone(publishedAt, timezone));
            return this;
        }

        public NewPublicationFormBuilder withAllowRegistration(boolean allowRegistration) {
            form.setAllowRegistration(allowRegistration ? "on" : null);
            return this;
        }

        public NewPublicationForm get() {
            return form;
        }
    }

}
