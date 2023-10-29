package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeoJsonFileWriter {

    public void writeGeoJsonFile(GPXPath gpxPath, File target) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(getCollectionHeader());
        sb.append(getGeojsonLinestringHeader());

        List<Point> points = gpxPath.getPoints();
        for (int i = 0; i < points.size(); i++) {

            sb.append("[")
                    .append(points.get(i).getLonDeg())
                    .append(", ")
                    .append(points.get(i).getLatDeg())
                    .append("]");

            if (i != points.size() - 1) {
                sb.append(",");
            }
        }

        sb.append(getGeojsonFooter()).append(",");

        Point firstPoint = gpxPath.getPoints().get(0);
        sb.append(getGeojsonPointHeader("start"));
        sb.append(firstPoint.getLonDeg())
                .append(", ")
                .append(firstPoint.getLatDeg());
        sb.append(getGeojsonFooter()).append(",");

        Point lastPoint = gpxPath.getPoints().get(gpxPath.getPoints().size() - 1);
        sb.append(getGeojsonPointHeader("end"));
        sb.append(lastPoint.getLonDeg())
                .append(", ")
                .append(lastPoint.getLatDeg());
        sb.append(getGeojsonFooter()).append(",");

        getDistanceMarkers(gpxPath, 10).stream().forEach(dm -> {
            sb.append(getGeojsonPointHeader(dm.get("label").toString()));
            sb.append(dm.get("lng"))
                    .append(", ")
                    .append(dm.get("lat"));
            sb.append(getGeojsonFooter()).append(",");
        });

        sb.setLength(sb.length() - 1); // remove last comma

        sb.append(getCollectionFooter());

        FileWriter fw = new FileWriter(target);
        fw.write(sb.toString());
        fw.close();

    }

    private List<java.util.Map<String, Object>> getDistanceMarkers(GPXPath gpxPath, int interval) {

        int intervalInMeters = Math.round(interval * 1000);
        int intervalCopy = intervalInMeters;

        List<java.util.Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < gpxPath.getPoints().size(); i++) {
            Point point = gpxPath.getPoints().get(i);
            if (point.getDist() >= intervalCopy) {
                result.add(
                        java.util.Map.of("index", i,
                                "label", Math.round(intervalCopy / 1000),
                                "lat", point.getLatDeg(),
                                "lng", point.getLonDeg())
                );

                intervalCopy += intervalInMeters;
            }
        }
        return result;
    }

    private String getCollectionHeader() {
        return "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [";
    }

    private String getCollectionFooter() {
        return "]\n" +
                "}";
    }

    private String getGeojsonLinestringHeader() {
        return "{\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"LineString\",\n" +
                "        \"coordinates\": [\n";
    }

    private String getGeojsonPointHeader(String label) {
        return "{\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\"name\": \"" + label + "\"},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n";
    }

    private String getGeojsonFooter() {
        return "]\n" +
                "      }\n" +
                "    }";
    }

}
