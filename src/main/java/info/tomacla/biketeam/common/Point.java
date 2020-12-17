package info.tomacla.biketeam.common;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Point {

    private double lat;
    private double lng;

    private Point() {

    }

    public Point(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.lat, lat) == 0 && Double.compare(point.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }

}
