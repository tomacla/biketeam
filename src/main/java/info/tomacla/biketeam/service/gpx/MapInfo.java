package info.tomacla.biketeam.service.gpx;

public record MapInfo(
        String name,
        double dist,
        double positiveElevation,
        double negativeElevation
) {
}
