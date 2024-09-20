package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.datatype.Dates;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class SearchFeedForm {

    private String from = "";
    private String to = "";

    private boolean onlyMyFeed = true;


    public static SearchFeedFormBuilder builder() {
        return new SearchFeedFormBuilder();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = Objects.requireNonNullElse(from, "");
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = Objects.requireNonNullElse(to, "");
    }

    public boolean isOnlyMyFeed() {
        return onlyMyFeed;
    }

    public void setOnlyMyFeed(boolean onlyMyFeed) {
        this.onlyMyFeed = onlyMyFeed;
    }

    public SearchFeedFormParser parser() {
        return new SearchFeedFormParser(this);
    }

    public static class SearchFeedFormParser {

        private final SearchFeedForm form;

        protected SearchFeedFormParser(SearchFeedForm form) {
            this.form = form;
        }

        public LocalDate getFrom() {
            if (form.getFrom() == null || form.getFrom().isBlank()) {
                return null;
            }
            return LocalDate.parse(form.getFrom());
        }

        public LocalDate getTo() {
            if (form.getTo() == null || form.getTo().isBlank()) {
                return null;
            }
            return LocalDate.parse(form.getTo());
        }

        public boolean isOnlyMyFeed() {
            return form.isOnlyMyFeed();
        }

    }

    public static class SearchFeedFormBuilder {

        private final SearchFeedForm form;

        protected SearchFeedFormBuilder() {
            this.form = new SearchFeedForm();
        }


        public SearchFeedFormBuilder withFrom(LocalDate from) {
            if (from != null) {
                form.setFrom(Dates.formatDate(from));
            }
            return this;
        }

        public SearchFeedFormBuilder withTo(LocalDate to) {
            if (to != null) {
                form.setTo(Dates.formatDate(to));
            }
            return this;
        }

        public SearchFeedFormBuilder withOnlyMyFeed(boolean onlyMyFeed) {
            form.setOnlyMyFeed(onlyMyFeed);
            return this;
        }

        public SearchFeedForm get() {
            return form;
        }

    }

}
