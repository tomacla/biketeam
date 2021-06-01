package info.tomacla.biketeam.common;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class Gpx {

    private static final String PARSER_URL = "https://n-peloton.fr/api/java/gpxinfo";
    private static final String SIMPLIFY_URL = "https://n-peloton.fr/api/java/simplify";
    private static final String FIT_CONVERTER_URL = "https://n-peloton.fr/api/java/fit";
    private static final String STATIC_IMAGE_GENERATOR_URL = "https://n-peloton.fr/api/java/map";

    public static Path staticImage(Path gpx, String mapBoxApiKey) {

        try {

            return generateStaticMap(gpx, mapBoxApiKey);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Path simplify(Path gpx, String name) {

        try {

            return simplifyGpx(gpx, name);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Path fit(Path gpx, String name) {

        try {

            return convertToFit(gpx, name);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static GpxDescriptor parse(Path gpx, String defaultName) {

        try {

            final GpxParserResponse gpxParserResponse = parseGpxInfo(gpx);
            final JpxParserResponse jpxParserResponse = parseGpxPoints(gpx, defaultName);

            return new GpxDescriptor(
                    jpxParserResponse.getName(),
                    jpxParserResponse.getStart(),
                    jpxParserResponse.getEnd(),
                    Rounder.round1Decimal(gpxParserResponse.getDistance()),
                    Rounder.round1Decimal(gpxParserResponse.getPositiveElevation()),
                    Rounder.round1Decimal(gpxParserResponse.getNegativeElevation()),
                    gpxParserResponse.isCrossing(),
                    new Vector(gpxParserResponse.getWind().get("x"), gpxParserResponse.getWind().get("y")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static JpxParserResponse parseGpxPoints(Path gpx, String defaultName) throws IOException {

        GPX readGpx = GPX.read(gpx);

        Optional<Track> optionalTrack = readGpx.tracks().findFirst();
        if (optionalTrack.isEmpty()) {
            throw new IllegalStateException("No tracks in GPX file");
        }

        final Track track = optionalTrack.get();

        Optional<TrackSegment> optionalSegment = track.getSegments().stream().findFirst();
        if (optionalSegment.isEmpty()) {
            throw new IllegalStateException("No segments in GPX file");
        }

        final TrackSegment segment = optionalSegment.get();

        return new JpxParserResponse(
                new Point(segment.getPoints().get(0).getLatitude().doubleValue(), segment.getPoints().get(0).getLongitude().doubleValue()),
                new Point(segment.getPoints().get(segment.getPoints().size() - 1).getLatitude().doubleValue(), segment.getPoints().get(segment.getPoints().size() - 1).getLongitude().doubleValue()),
                track.getName().orElse(defaultName)
        );

    }

    private static Path generateStaticMap(Path gpx, String mapBoxApiKey) throws IOException {

        String tileUrl = mapBoxApiKey != null ? "https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=" + mapBoxApiKey : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> gpxHeaderMap = new LinkedMultiValueMap<>();
        gpxHeaderMap.add("Content-disposition", "form-data; name=file; filename=" + gpx.getFileName().toString());
        gpxHeaderMap.add("Content-type", "application/gpx+xml");
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(gpx), gpxHeaderMap);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("filex", fileEntity);
        body.add("width", "768");
        body.add("height", "512");
        body.add("tileUrl", tileUrl);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(body, headers);
        byte[] imageBytes = new RestTemplate(requestFactory()).postForObject(STATIC_IMAGE_GENERATOR_URL, reqEntity, byte[].class);

        Path staticMap = Files.createTempFile("staticmap", ".png");
        Files.write(staticMap, imageBytes);

        return staticMap;

    }

    private static GpxParserResponse parseGpxInfo(Path gpx) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> gpxHeaderMap = new LinkedMultiValueMap<>();
        gpxHeaderMap.add("Content-disposition", "form-data; name=file; filename=" + gpx.getFileName().toString());
        gpxHeaderMap.add("Content-type", "application/gpx+xml");
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(gpx), gpxHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("filex", fileEntity);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        ResponseEntity<GpxParserResponse> resE = new RestTemplate(requestFactory()).exchange(PARSER_URL, HttpMethod.POST, reqEntity, GpxParserResponse.class);

        return resE.getBody();

    }

    private static Path simplifyGpx(Path gpx, String name) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> gpxHeaderMap = new LinkedMultiValueMap<>();
        gpxHeaderMap.add("Content-disposition", "form-data; name=file; filename=" + gpx.getFileName().toString());
        gpxHeaderMap.add("Content-type", "application/gpx+xml");
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(gpx), gpxHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("filex", fileEntity);
        multipartReqMap.add("name", name);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        byte[] simplifiedBytes = new RestTemplate(requestFactory()).postForObject(SIMPLIFY_URL, reqEntity, byte[].class);

        Path simplified = Files.createTempFile("gpx-simplified", ".gpx");
        Files.write(simplified, simplifiedBytes);

        return simplified;

    }

    private static Path convertToFit(Path gpx, String name) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> gpxHeaderMap = new LinkedMultiValueMap<>();
        gpxHeaderMap.add("Content-disposition", "form-data; name=file; filename=" + gpx.getFileName().toString());
        gpxHeaderMap.add("Content-type", "application/gpx+xml");
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(gpx), gpxHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("filex", fileEntity);
        multipartReqMap.add("name", name);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        byte[] simplifiedBytes = new RestTemplate(requestFactory()).postForObject(FIT_CONVERTER_URL, reqEntity, byte[].class);

        Path simplified = Files.createTempFile("gpx-simplified", ".fit");
        Files.write(simplified, simplifiedBytes);

        return simplified;

    }

    public static class GpxDescriptor {

        private final String name;
        private final Point start;
        private final Point end;
        private final double length;
        private final double positiveElevation;
        private final double negativeElevation;
        private final boolean crossing;
        private final Vector wind;

        public GpxDescriptor(String name, Point start, Point end, double length, double positiveElevation, double negativeElevation,
                             boolean crossing, Vector wind) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.length = length;
            this.positiveElevation = positiveElevation;
            this.negativeElevation = negativeElevation;
            this.crossing = crossing;
            this.wind = wind;

        }

        public String getName() {
            return name;
        }

        public Point getStart() {
            return start;
        }

        public Point getEnd() {
            return end;
        }

        public double getLength() {
            return length;
        }

        public double getPositiveElevation() {
            return positiveElevation;
        }

        public double getNegativeElevation() {
            return negativeElevation;
        }

        public boolean isCrossing() {
            return crossing;
        }

        public Vector getWind() {
            return wind;
        }

    }

    private static class GpxParserResponse {

        private float distance;

        private int positiveElevation;

        private int negativeElevation;

        private Map<String, Double> wind;

        private boolean crossing;

        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }

        public int getPositiveElevation() {
            return positiveElevation;
        }

        public void setPositiveElevation(int positiveElevation) {
            this.positiveElevation = positiveElevation;
        }

        public int getNegativeElevation() {
            return negativeElevation;
        }

        public void setNegativeElevation(int negativeElevation) {
            this.negativeElevation = negativeElevation;
        }

        public Map<String, Double> getWind() {
            return wind;
        }

        public void setWind(Map<String, Double> wind) {
            this.wind = wind;
        }

        public boolean isCrossing() {
            return crossing;
        }

        public void setCrossing(boolean crossing) {
            this.crossing = crossing;
        }
    }

    private static class JpxParserResponse {

        private final Point start;
        private final Point end;
        private final String name;

        public JpxParserResponse(Point start, Point end, String name) {
            this.start = start;
            this.end = end;
            this.name = name;
        }

        public Point getStart() {
            return start;
        }

        public Point getEnd() {
            return end;
        }

        public String getName() {
            return name;
        }
    }

    // FIXME this is unsecured
    private static ClientHttpRequestFactory requestFactory() {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

}
