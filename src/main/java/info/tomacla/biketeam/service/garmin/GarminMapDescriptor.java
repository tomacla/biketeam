package info.tomacla.biketeam.service.garmin;

import info.tomacla.biketeam.domain.map.MapType;
import io.github.glandais.gpx.data.GPX;

public record GarminMapDescriptor(GPX gpx, MapType type) {

}
