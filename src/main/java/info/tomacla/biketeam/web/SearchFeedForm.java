package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.datatype.Dates;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SearchFeedForm {

    private String from = Dates.formatDate(LocalDate.now().minus(1, ChronoUnit.MONTHS));
    private String to = Dates.formatDate(LocalDate.now().plus(3, ChronoUnit.MONTHS));

    private boolean includeTrips = true;

    private boolean includeRides = true;

    private boolean includePublications = true;

    public static SearchFeedFormBuilder builder() {
        return new SearchFeedFormBuilder();
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
        this.to = Objects.requireNonNullElse(to, Dates.formatDate(LocalDate.now().plus(3, ChronoUnit.MONTHS)));
    }

    public boolean getIncludeTrips() {
        return includeTrips;
    }

    public void setIncludeTrips(boolean includeTrips) {
        this.includeTrips = includeTrips;
    }

    public boolean getIncludeRides() {
        return includeRides;
    }

    public void setIncludeRides(boolean includeRides) {
        this.includeRides = includeRides;
    }

    public boolean getIncludePublications() {
        return includePublications;
    }

    public void setIncludePublications(boolean includePublications) {
        this.includePublications = includePublications;
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
            return LocalDate.parse(form.getFrom());
        }

        public LocalDate getTo() {
            return LocalDate.parse(form.getTo());
        }

        public boolean isIncludePublications() {
            return form.getIncludePublications();
        }

        public boolean isIncludeTrips() {
            return form.getIncludeTrips();
        }

        public boolean isIncludeRides() {
            return form.getIncludeRides();
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

        public SearchFeedFormBuilder withIncludeTrips(boolean includeTrips) {
            form.setIncludeTrips(includeTrips);
            return this;
        }

        public SearchFeedFormBuilder withIncludeRides(boolean includeRides) {
            form.setIncludeRides(includeRides);
            return this;
        }

        public SearchFeedFormBuilder withIncludePublications(boolean includePublications) {
            form.setIncludePublications(includePublications);
            return this;
        }

        public SearchFeedForm get() {
            return form;
        }

    }

}
