package info.tomacla.biketeam.domain.map;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapSorterTest {

    @Test
    public void test() {

        Map shortM = new Map();
        shortM.setId("short");
        shortM.setLength(50.0);
        shortM.setPositiveElevation(3000.0);

        Map longM = new Map();
        longM.setId("long");
        longM.setLength(100.0);
        longM.setPositiveElevation(1000);

        List<Map> maps = Arrays.asList(shortM, longM);

        maps.sort(MapSorter.of(MapSorterOption.SHORT));
        assertEquals(maps.get(0), shortM);

        maps.sort(MapSorter.of(MapSorterOption.LONG));
        assertEquals(maps.get(0), longM);

        maps.sort(MapSorter.of(MapSorterOption.FLAT));
        assertEquals(maps.get(0), longM);

        maps.sort(MapSorter.of(MapSorterOption.HILLY));
        assertEquals(maps.get(0), shortM);

    }
}
