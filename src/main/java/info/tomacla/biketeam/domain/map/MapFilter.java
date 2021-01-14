package info.tomacla.biketeam.domain.map;

public class MapFilter {

    public static boolean byWind(Map map, WindDirection windDirection) {

        double angle = Math.toRadians(90 - windDirection.angle);
        double x1 = Math.cos(angle);
        double y1 = Math.sin(angle) * -1;
        double x2 = map.getWindVector().getX();
        double y2 = map.getWindVector().getY();
        double dotProduct = (x1 * x2) + (y1 * y2);
        double score = 0.5 + (0.5 * dotProduct);
        return score > 0.3;
    }

}
