package info.tomacla.biketeam.service.gpx;

import io.github.glandais.gpx.climb.Climb;

import java.util.List;

public record MapData(MapInfo info, List<MapPoint> points, List<Climb> climbs, List<Marker> markers) {

}
