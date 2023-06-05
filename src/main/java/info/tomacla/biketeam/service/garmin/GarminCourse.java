package info.tomacla.biketeam.service.garmin;

import java.util.List;

public class GarminCourse {

    String courseName;

    Long courseId;

    double distance;

    double elevationGain;

    double elevationLoss;

    List<GarminCourseGeoPoint> geoPoints;

    GarminCourseActivityType activityType;

    String coordinateSystem;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getElevationGain() {
        return elevationGain;
    }

    public void setElevationGain(double elevationGain) {
        this.elevationGain = elevationGain;
    }

    public double getElevationLoss() {
        return elevationLoss;
    }

    public void setElevationLoss(double elevationLoss) {
        this.elevationLoss = elevationLoss;
    }

    public List<GarminCourseGeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public void setGeoPoints(List<GarminCourseGeoPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }

    public GarminCourseActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(GarminCourseActivityType activityType) {
        this.activityType = activityType;
    }

    public String getCoordinateSystem() {
        return coordinateSystem;
    }

    public void setCoordinateSystem(String coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }
}
