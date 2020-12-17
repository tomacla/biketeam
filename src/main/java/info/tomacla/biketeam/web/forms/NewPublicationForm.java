package info.tomacla.biketeam.web.forms;

import info.tomacla.biketeam.domain.publication.Publication;
import org.springframework.web.multipart.MultipartFile;

public class NewPublicationForm {

    private String id;
    private String title;
    private String content;
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
        return form;
    }

}
