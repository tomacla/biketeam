package info.tomacla.biketeam.web.map;

public class RatingResponse {
    public final Double averageRating;
    public final Long ratingCount;
    public final Integer userRating;

    public RatingResponse(Double averageRating, Long ratingCount, Integer userRating) {
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.userRating = userRating;
    }
}
