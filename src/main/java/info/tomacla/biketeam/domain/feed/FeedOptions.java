package info.tomacla.biketeam.domain.feed;

import java.time.LocalDate;

public class FeedOptions {

    private LocalDate from = null;
    private LocalDate to = null;
    private boolean onlyMyFeed = false;

    public FeedOptions() {
    }

    public FeedOptions(LocalDate from, LocalDate to, boolean onlyMyFeed) {
        this.from = from;
        this.to = to;
        this.onlyMyFeed = onlyMyFeed;
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

    public boolean isOnlyMyFeed() {
        return onlyMyFeed;
    }

    public void setOnlyMyFeed(boolean onlyMyFeed) {
        this.onlyMyFeed = onlyMyFeed;
    }
}
