package info.tomacla.biketeam.service.garmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GarminCourseService {

    @Autowired
    private GarminAuthService garminAuthService;

    @Autowired
    private GPXParser gpxParser;

    @Autowired
    private ObjectMapper objectMapper;

    protected GarminCourse convert(GarminMapDescriptor descriptor) throws Exception {
        GarminCourse result = new GarminCourse();
        result.setCourseName(descriptor.courseName());
        result.setDistance(1000.0 * descriptor.length());
        result.setElevationGain(descriptor.positiveElevation());
        result.setElevationLoss(-descriptor.negativeElevation());
        switch (descriptor.type()) {
            case GRAVEL -> result.setActivityType(GarminCourseActivityType.GRAVEL_CYCLING);
            case MTB -> result.setActivityType(GarminCourseActivityType.MOUNTAIN_BIKING);
            default -> result.setActivityType(GarminCourseActivityType.ROAD_CYCLING);
        }

        result.setCoordinateSystem("WGS84");
        GPXPath gpxPath = gpxParser.parsePaths(descriptor.gpxFilePath().toFile()).get(0);
        List<GarminCourseGeoPoint> geoPoints = gpxPath.getPoints().stream()
                .map(p -> new GarminCourseGeoPoint(
                        p.getLatDeg(),
                        p.getLonDeg(),
                        p.getEle()
                ))
                .collect(Collectors.toList());
        result.setGeoPoints(geoPoints);
        return result;
    }

    public String upload(HttpServletRequest request,
                         HttpServletResponse response,
                         HttpSession session,
                         GarminToken garminToken,
                         GarminMapDescriptor descriptor) throws Exception {
        GarminCourse garminCourse = convert(descriptor);
        String json = objectMapper.writeValueAsString(garminCourse);
        OAuthRequest oauthRequest = new OAuthRequest(Verb.POST, "https://apis.garmin.com/training-api/courses/v1/course");
        oauthRequest.setPayload(json);
        oauthRequest.addHeader("Content-Type", "application/json");
        String body = garminAuthService.execute(request, response, session, garminToken, oauthRequest);
        if (body != null) {
            GarminCourse result = objectMapper.readValue(body, GarminCourse.class);
            return "https://connect.garmin.com/modern/course/" + result.getCourseId();
        } else {
            return null;
        }
    }

}
