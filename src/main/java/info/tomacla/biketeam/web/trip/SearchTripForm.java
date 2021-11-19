package info.tomacla.biketeam.web.trip;

import info.tomacla.biketeam.common.datatype.Dates;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SearchTripForm {

    private int page;
    private int pageSize;
    private String from;
    private String to;

    public SearchTripForm() {
        setPage(0);
        setPageSize(10);
        setTo(null);
        setFrom(null);
    }

    public static SearchTripFormBuilder builder() {
        return new SearchTripFormBuilder();
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

    public SearchTripFormParser parser() {
        return new SearchTripFormParser(this);
    }

    public static class SearchTripFormParser {

        private final SearchTripForm form;

        protected SearchTripFormParser(SearchTripForm form) {
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

    public static class SearchTripFormBuilder {

        private final SearchTripForm form;

        protected SearchTripFormBuilder() {
            this.form = new SearchTripForm();
        }

        public SearchTripFormBuilder withPage(int page) {
            form.setPage(page);
            return this;
        }

        public SearchTripFormBuilder withPageSize(int pageSize) {
            form.setPageSize(pageSize);
            return this;
        }

        public SearchTripFormBuilder withFrom(LocalDate from) {
            if (from != null) {
                form.setFrom(Dates.formatDate(from));
            }
            return this;
        }

        public SearchTripFormBuilder withTo(LocalDate to) {
            if (to != null) {
                form.setTo(Dates.formatDate(to));
            }
            return this;
        }

        public SearchTripForm get() {
            return form;
        }

    }

}
