package info.tomacla.biketeam.service.gpx;

public record Marker(
        double lat,
        double lon,
        String type,
        String label
) {
}
