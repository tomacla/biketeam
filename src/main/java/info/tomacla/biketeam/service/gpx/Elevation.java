package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.Point;

import java.util.List;

public class Elevation {
    private Point startPoint;
    private Point endPoint;
    private double positiveElevation;


    public Elevation(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.positiveElevation = endPoint.getEle() - startPoint.getEle();

    }

    public Elevation(Point startPoint, Point endPoint, double positiveElevation) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.positiveElevation = positiveElevation;
    }

    public void add(Point p) {
        this.positiveElevation += p.getEle() - this.endPoint.getEle();
        this.endPoint = p;
    }

    public double getAverageGrade() {
        return (this.positiveElevation / this.getDistance()) * 100.0D;
    }

    public double getPositiveElevation() {
        return positiveElevation;
    }

    public double getDistance() {
        return endPoint.getDist() - startPoint.getDist();
    }

    public boolean isInside(Point p) {
        return p.getDist() >= startPoint.getDist() && p.getDist() <= endPoint.getDist();
    }

    public boolean isRelevant() {

        if (getAverageGrade() >= 4.0D) {
            return true;
        }

        if (getAverageGrade() >= 3.0D && getDistance() >= 300.0D) {
            return true;
        }

        if (getAverageGrade() >= 2.0D && getDistance() >= 500.0D) {
            return true;
        }

        if (getAverageGrade() >= 1.0D && getDistance() >= 750.0D) {
            return true;
        }

        if (getAverageGrade() >= 0.5D && getDistance() >= 1000.0D) {
            return true;
        }

        return false;
    }

    public String getColor() {

        if (getAverageGrade() >= 5.0D) {
            return "rgb(242,78,78)";
        }

        if (getAverageGrade() >= 3.0D && getDistance() >= 150.0D) {
            return "rgb(247,129,69)";
        }

        if (getAverageGrade() >= 2.0D && getDistance() >= 350.0D || getAverageGrade() >= 1 && getDistance() >= 600.0D) {
            return "rgb(252,223,62)";
        }

        return getDefaultColor();

    }

    public static String getDefaultColor() {
        return "rgb(160,176,70)";
    }

    public static List<Elevation> mergeIfNear(List<Elevation> elevations) {
        boolean modified = true;
        while (modified) {

            modified = false;
            for (int i = 1; i < elevations.size(); i++) {

                Elevation previous = elevations.get(i - 1);
                Elevation current = elevations.get(i);
                if (Elevation.isNear(previous, current)) {
                    elevations.set(i - 1, Elevation.merge(previous, current));
                    elevations.remove(i);
                    modified = true;
                    break;
                }

            }

        }
        return elevations;
    }

    public static boolean isNear(Elevation e1, Elevation e2) {
        return e2.startPoint.getDist() - e1.endPoint.getDist() <= 50.0D;
    }

    public static Elevation merge(Elevation previous, Elevation current) {

        return new Elevation(
                previous.startPoint,
                current.endPoint,
                current.positiveElevation + previous.positiveElevation
        );

    }

}
