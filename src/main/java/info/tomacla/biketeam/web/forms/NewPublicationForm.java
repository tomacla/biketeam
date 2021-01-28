package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.publication.Publication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NewPublicationForm {

    private String id;
    private String title;
    private String content;
    private String publishedAtDate;
    private String publishedAtTime;
    private MultipartFile file;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean fileSet() {
        return file != null && !file.isEmpty();
    }

    public static NewPublicationForm build(Publication obj) {
        NewPublicationForm form = new NewPublicationForm();
        form.id = obj.getId();
        form.title = obj.getTitle();
        form.content = obj.getContent();
        form.publishedAtDate = obj.getPublishedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        form.publishedAtTime = obj.getPublishedAt().toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
        return form;
    }

    public static NewPublicationForm empty() {
        NewPublicationForm form = new NewPublicationForm();
        form.setPublishedAtDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setPublishedAtTime(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return form;
    }

}
