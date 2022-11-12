package info.tomacla.biketeam.domain.feed;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FeedOptions {

    private boolean includePublications = true;
    private boolean includeTrips = true;
    private boolean includeRides = true;
    private LocalDate from = LocalDate.now().minus(1, ChronoUnit.MONTHS);
    private LocalDate to = LocalDate.now().plus(3, ChronoUnit.MONTHS);

    public FeedOptions() {
    }

    public FeedOptions(boolean includePublications, boolean includeTrips, boolean includeRides, LocalDate from, LocalDate to) {
        this.includePublications = includePublications;
        this.includeTrips = includeTrips;
        this.includeRides = includeRides;
        this.from = from;
        this.to = to;
    }

    public boolean isIncludePublications() {
        return includePublications;
    }

    public void setIncludePublications(boolean includePublications) {
        this.includePublications = includePublications;
    }

    public boolean isIncludeTrips() {
        return includeTrips;
    }

    public void setIncludeTrips(boolean includeTrips) {
        this.includeTrips = includeTrips;
    }

    public boolean isIncludeRides() {
        return includeRides;
    }

    public void setIncludeRides(boolean includeRides) {
        this.includeRides = includeRides;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

}
