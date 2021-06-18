package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Vector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapSorterTest {

    @Test
    public void test() {

        Map shortM = new Map("team",
                "1",
                "short",
                50.0,
                MapType.ROAD,
                100.0,
                -100.0,
                new ArrayList<>(),
                new Point(10.0,5.0),
                new Point(5.0, 10.0),
                WindDirection.NORTH,
                false,
                false);

        Map longM = new Map("team","2","long",
                100.0,
                MapType.ROAD,
                100.0,
                -100.0,
                new ArrayList<>(),
                new Point(10.0,5.0),
                new Point(5.0, 10.0),
                WindDirection.NORTH,
                false,
                false);

        List<Map> maps = Arrays.asList(shortM, longM);

        maps.sort(MapSorter.of(MapSorterOption.SHORT));
        assertEquals(maps.get(0), shortM);

        maps.sort(MapSorter.of(MapSorterOption.LONG));
        assertEquals(maps.get(0), longM);

    }
}
