package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Vector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapFilterTest {

    @Test
    public void test() {

        Map north = new Map("north",
                50.0,
                100.0,
                -100.0,
                new ArrayList<>(),
                new Point(10.0,5.0),
                new Point(5.0, 10.0),
                new Vector(5.0, -3.0),
                false,
                false);

        assertTrue(MapFilter.byWind(north, WindDirection.NORTH));
    }

}
