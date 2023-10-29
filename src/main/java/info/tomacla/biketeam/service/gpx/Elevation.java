package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.Point;

import java.util.List;

public class Elevation {

    final private List<Point> points;

    public Elevation(List<Point> points) {
        this.points = points;
    }

    public double getAverageGrade() {
        return (getLastPoint().getEle() - getFirstPoint().getEle()) / getDist() * 100.0D;
    }

    public double getDist() {
        return getLastPoint().getDist() - getFirstPoint().getDist();
    }

    private Point getFirstPoint() {
        return points.get(0);
    }

    private Point getLastPoint() {
        return points.get(points.size() - 1);
    }

    public boolean isInside(Point p) {
        return p.getDist() >= getFirstPoint().getDist() && p.getDist() <= getLastPoint().getDist();
    }

    public boolean isRelevant() {

        if (getAverageGrade() >= 4) {
            return true;
        }

        if (getAverageGrade() >= 3 && getDist() >= 300.0D) {
            return true;
        }

        if (getAverageGrade() >= 2 && getDist() >= 500.0D) {
            return true;
        }

        if (getAverageGrade() >= 1 && getDist() >= 1000.0D) {
            return true;
        }

        return false;
    }

    public String getColor() {

        if (getAverageGrade() >= 5.0D) {
            return "rgb(242,78,78)";
        }

        if (getAverageGrade() >= 3.0D && getDist() >= 150.0D) {
            return "rgb(247,129,69)";
        }

        if (getAverageGrade() >= 2.0D && getDist() >= 350.0D || getAverageGrade() >= 1 && getDist() >= 600.0D) {
            return "rgb(252,223,62)";
        }

        return getDefaultColor();

    }

    public static String getDefaultColor() {
        return "rgb(160,176,70)";
    }

}
