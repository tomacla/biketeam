package info.tomacla.biketeam.service.gpx;

public record MapPoint(
        int index,
        double lat,
        double lon,
        double dist,
        double ele,
        double grade,
        Integer climbIndex,
        Double simplifiedClimbGrade
) {

}
