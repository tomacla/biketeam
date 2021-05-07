package info.tomacla.biketeam.web.forms;

import org.apache.tomcat.jni.Local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SearchRideForm {

    private int page;
    private int pageSize;
    private String from;
    private String to;

    public SearchRideForm() {
        setPage(0);
        setPageSize(10);
        setTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        setFrom(LocalDate.now().minus(1, ChronoUnit.MONTHS).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public static SearchRideFormParser parser(SearchRideForm form) {
        return new SearchRideFormParser(form);
    }

    public static SearchRideFormBuilder builder() {
        return new SearchRideFormBuilder();
    }

    public static class SearchRideFormParser {

        private SearchRideForm form;

        protected SearchRideFormParser(SearchRideForm form) {
            this.form = form;
        }

        public int getPage() {
            return form.getPage();
        }

        public int getPageSize() {
            return form.getPageSize();
        }

        public LocalDate getFrom() {
            return LocalDate.parse(form.getFrom());
        }

        public LocalDate getTo() {
            return LocalDate.parse(form.getTo());
        }

    }

    public static class SearchRideFormBuilder {

        private SearchRideForm form;

        protected SearchRideFormBuilder() {
            this.form = new SearchRideForm();
        }

        public SearchRideFormBuilder withFrom(LocalDate from) {
            form.setFrom(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return this;
        }

        public SearchRideFormBuilder withTo(LocalDate to) {
            form.setTo(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return this;
        }

        public SearchRideForm get() {
            return form;
        }

    }

}
