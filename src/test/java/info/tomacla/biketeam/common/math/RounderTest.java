package info.tomacla.biketeam.common.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RounderTest {

    @Test
    public void test() {
        assertEquals(25, Rounder.round(25.3));
        assertEquals(25.4, Rounder.round1Decimal(25.352632));
        assertEquals(25.35, Rounder.round2Decimals(25.352632));
    }

}
