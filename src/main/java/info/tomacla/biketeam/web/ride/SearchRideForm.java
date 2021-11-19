package info.tomacla.biketeam.web.ride;

import info.tomacla.biketeam.common.datatype.Dates;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SearchRideForm {

    private int page;
    private int pageSize;
    private String from;
    private String to;

    public SearchRideForm() {
        setPage(0);
        setPageSize(10);
        setTo(null);
        setFrom(null);
    }

    public static SearchRideFormBuilder builder() {
        return new SearchRideFormBuilder();
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
        this.from = Objects.requireNonNullElse(from, Dates.formatDate(LocalDate.now().minus(1, ChronoUnit.MONTHS)));
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = Objects.requireNonNullElse(to, Dates.formatDate(LocalDate.now().plus(1, ChronoUnit.MONTHS)));
    }

    public SearchRideFormParser parser() {
        return new SearchRideFormParser(this);
    }

    public static class SearchRideFormParser {

        private final SearchRideForm form;

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

        private final SearchRideForm form;

        protected SearchRideFormBuilder() {
            this.form = new SearchRideForm();
        }

        public SearchRideFormBuilder withPage(int page) {
            form.setPage(page);
            return this;
        }

        public SearchRideFormBuilder withPageSize(int pageSize) {
            form.setPageSize(pageSize);
            return this;
        }

        public SearchRideFormBuilder withFrom(LocalDate from) {
            if (from != null) {
                form.setFrom(Dates.formatDate(from));
            }
            return this;
        }

        public SearchRideFormBuilder withTo(LocalDate to) {
            if (to != null) {
                form.setTo(Dates.formatDate(to));
            }
            return this;
        }

        public SearchRideForm get() {
            return form;
        }

    }

}
