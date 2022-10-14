package info.tomacla.biketeam.common.file;

import java.util.List;

public interface FileRepositories {

    String RIDE_IMAGES = "ride-images";
    String PUBLICATION_IMAGES = "pub-images";
    String TRIP_IMAGES = "trip-images";
    String MAP_IMAGES = "map-images";
    String GPX_FILES = "gpx";
    String FIT_FILES = "fit";
    String GEOJSON_FILES = "geojson";
    String MISC_IMAGES = "misc";
    String USER_IMAGES = "user-images";

    static List<String> list() {
        return List.of(RIDE_IMAGES,
                PUBLICATION_IMAGES,
                TRIP_IMAGES,
                MAP_IMAGES,
                GPX_FILES,
                FIT_FILES,
                GEOJSON_FILES,
                MISC_IMAGES,
                USER_IMAGES);
    }


}
