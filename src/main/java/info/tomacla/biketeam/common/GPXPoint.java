package info.tomacla.biketeam.common;

import java.time.ZonedDateTime;
import java.util.Objects;

public class GPXPoint extends Point {

    private final double elevation;
    private final ZonedDateTime time;

    public GPXPoint(double lat, double lng, double elevation, ZonedDateTime time) {
        super(lat, lng);
        this.elevation = elevation;
        this.time = Objects.requireNonNull(time);
    }

    public double getElevation() {
        return elevation;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GPXPoint gpxPoint = (GPXPoint) o;
        return Double.compare(gpxPoint.elevation, elevation) == 0 && time.equals(gpxPoint.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elevation, time);
    }

}
