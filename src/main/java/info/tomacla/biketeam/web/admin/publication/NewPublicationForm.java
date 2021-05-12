package info.tomacla.biketeam.web.admin.publication;

import info.tomacla.biketeam.common.Strings;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class NewPublicationForm {

    private String id;
    private String title;
    private String content;
    private String publishedAtDate;
    private String publishedAtTime;
    private MultipartFile file;

    public NewPublicationForm() {
        setId(null);
        setTitle(null);
        setContent(null);
        setPublishedAtDate(null);
        setPublishedAtTime(null);
        setFile(null);
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
        this.publishedAtDate = Strings.requireNonBlankOrDefault(publishedAtDate, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public String getPublishedAtTime() {
        return publishedAtTime;
    }

    public void setPublishedAtTime(String publishedAtTime) {
        this.publishedAtTime = Strings.requireNonBlankOrDefault(publishedAtTime, LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME));
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

    public static NewPublicationFormBuilder builder() {
        return new NewPublicationFormBuilder();
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
            return form.getTitle();
        }

        public String getContent() {
            return form.getContent();
        }

        public ZonedDateTime getPublishedAt(ZoneId timezone) {
            return ZonedDateTime.of(LocalDateTime.parse(form.getPublishedAtDate() + "T" + form.getPublishedAtTime()), timezone);
        }

        public Optional<MultipartFile> getFile() {
            if (form.fileSet()) {
                return Optional.of(form.getFile());
            }
            return Optional.empty();
        }

    }

    public static class NewPublicationFormBuilder {

        private final NewPublicationForm form;

        public NewPublicationFormBuilder() {
            this.form = new NewPublicationForm();
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

        public NewPublicationFormBuilder withPublishedAt(ZonedDateTime publishedAt) {
            form.setPublishedAtDate(publishedAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            form.setPublishedAtTime(publishedAt.truncatedTo(ChronoUnit.SECONDS).toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
            return this;
        }

        public NewPublicationForm get() {
            return form;
        }
    }

}
