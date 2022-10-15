package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class GeoJsonFileWriter {

    private static final String GEOJSON_HEADER = "{\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {},\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"LineString\",\n" +
            "        \"coordinates\": [\n";

    private static final String GEOJSON_FOOTER = "]\n" +
            "      }\n" +
            "    }";

    public void writeGeoJsonFile(GPXPath gpxPath, File target) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(GEOJSON_HEADER);

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

        sb.append(GEOJSON_FOOTER);

        FileWriter fw = new FileWriter(target);
        fw.write(sb.toString());
        fw.close();

    }

}
