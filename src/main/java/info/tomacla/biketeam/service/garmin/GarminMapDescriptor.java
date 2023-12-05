package info.tomacla.biketeam.service.garmin;

import info.tomacla.biketeam.domain.map.MapType;

import java.nio.file.Path;

public record GarminMapDescriptor(Path gpxFilePath, String courseName, MapType type, double length,
                                  double positiveElevation, double negativeElevation) {

}
