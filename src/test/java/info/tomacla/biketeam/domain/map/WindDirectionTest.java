package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.geo.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WindDirectionTest {

    @Test
    public void test() {
        assertEquals(WindDirection.findDirectionFromVector(new Vector(5.0, -3.0)), WindDirection.SOUTH_WEST);
    }

}
