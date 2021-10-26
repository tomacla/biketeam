package info.tomacla.biketeam.common;

import java.util.List;

public interface FileRepositories {

    String RIDE_IMAGES = "ride-images";
    String PUBLICATION_IMAGES = "pub-images";
    String MAP_IMAGES = "map-images";
    String GPX_FILES = "gpx";
    String FIT_FILES = "fit";
    String MISC_IMAGES = "misc";

    static List<String> list() {
        return List.of(RIDE_IMAGES,
                PUBLICATION_IMAGES,
                MAP_IMAGES,
                GPX_FILES,
                FIT_FILES,
                MISC_IMAGES);
    }


}
