package info.tomacla.biketeam.service.feed;

import java.time.LocalDate;
import java.util.Objects;

public class FeedItem {

    private final String id;
    private final FeedItemType type;
    private final LocalDate date;
    private final String title;
    private final String content;
    private final String detailsUrl;
    private final String imageUrl;

    public FeedItem(String id,
                    FeedItemType type,
                    LocalDate date,
                    String title,
                    String content,
                    String detailsUrl,
                    String imageUrl) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.date = Objects.requireNonNull(date);
        this.title = Objects.requireNonNull(title);
        this.content = Objects.requireNonNull(content);
        this.detailsUrl = detailsUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public FeedItemType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
