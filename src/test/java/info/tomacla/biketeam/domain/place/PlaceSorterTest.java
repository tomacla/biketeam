package info.tomacla.biketeam.domain.place;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaceSorterTest {

    @Test
    public void test() {

        PlaceAppearanceProjection p1 = Mockito.mock(PlaceAppearanceProjection.class);
        Mockito.when(p1.getName()).thenReturn("Place 1");
        Mockito.when(p1.getId()).thenReturn("place_1");
        Mockito.when(p1.getTeamId()).thenReturn("team_id1");
        Mockito.when(p1.getTripStartPlaceAppearances()).thenReturn(5);
        Mockito.when(p1.getTripEndPlaceAppearances()).thenReturn(6);
        Mockito.when(p1.getRideStartPlaceAppearances()).thenReturn(0);
        Mockito.when(p1.getRideEndPlaceAppearances()).thenReturn(0);
        Mockito.when(p1.getLastTripStartPlaceAppearance()).thenReturn(LocalDate.of(2022, 1, 5));
        Mockito.when(p1.getLastTripEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 2, 5));
        Mockito.when(p1.getLastRideStartPlaceAppearance()).thenReturn(LocalDate.of(2022, 3, 5));
        Mockito.when(p1.getLastRideEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 4, 5));

        PlaceAppearanceProjection p2 = Mockito.mock(PlaceAppearanceProjection.class);
        Mockito.when(p2.getName()).thenReturn("Place 2");
        Mockito.when(p2.getId()).thenReturn("place_2");
        Mockito.when(p2.getTeamId()).thenReturn("team_id1");
        Mockito.when(p2.getTripStartPlaceAppearances()).thenReturn(0);
        Mockito.when(p2.getTripEndPlaceAppearances()).thenReturn(0);
        Mockito.when(p2.getRideStartPlaceAppearances()).thenReturn(3);
        Mockito.when(p2.getRideEndPlaceAppearances()).thenReturn(8);
        Mockito.when(p2.getLastTripStartPlaceAppearance()).thenReturn(LocalDate.of(2022, 1, 5));
        Mockito.when(p2.getLastTripEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 2, 5));
        Mockito.when(p2.getLastRideStartPlaceAppearance()).thenReturn(LocalDate.of(2022, 3, 5));
        Mockito.when(p2.getLastRideEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 4, 5));

        PlaceAppearanceProjection p3 = Mockito.mock(PlaceAppearanceProjection.class);
        Mockito.when(p3.getName()).thenReturn("Place 3");
        Mockito.when(p3.getId()).thenReturn("place_3");
        Mockito.when(p3.getTeamId()).thenReturn("team_id1");
        Mockito.when(p3.getTripStartPlaceAppearances()).thenReturn(0);
        Mockito.when(p3.getTripEndPlaceAppearances()).thenReturn(0);
        Mockito.when(p3.getRideStartPlaceAppearances()).thenReturn(0);
        Mockito.when(p3.getRideEndPlaceAppearances()).thenReturn(0);
        Mockito.when(p3.getLastTripStartPlaceAppearance()).thenReturn(null);
        Mockito.when(p3.getLastTripEndPlaceAppearance()).thenReturn(null);
        Mockito.when(p3.getLastRideStartPlaceAppearance()).thenReturn(null);
        Mockito.when(p3.getLastRideEndPlaceAppearance()).thenReturn(null);

        PlaceAppearanceProjection p4 = Mockito.mock(PlaceAppearanceProjection.class);
        Mockito.when(p4.getName()).thenReturn("Place 4");
        Mockito.when(p4.getId()).thenReturn("place_4");
        Mockito.when(p4.getTeamId()).thenReturn("team_id1");
        Mockito.when(p4.getTripStartPlaceAppearances()).thenReturn(1);
        Mockito.when(p4.getTripEndPlaceAppearances()).thenReturn(4);
        Mockito.when(p4.getRideStartPlaceAppearances()).thenReturn(6);
        Mockito.when(p4.getRideEndPlaceAppearances()).thenReturn(0);
        Mockito.when(p4.getLastTripStartPlaceAppearance()).thenReturn(LocalDate.of(2021, 1, 5));
        Mockito.when(p4.getLastTripEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 2, 5));
        Mockito.when(p4.getLastRideStartPlaceAppearance()).thenReturn(LocalDate.of(2020, 3, 5));
        Mockito.when(p4.getLastRideEndPlaceAppearance()).thenReturn(LocalDate.of(2021, 4, 5));

        PlaceAppearanceProjection p5 = Mockito.mock(PlaceAppearanceProjection.class);
        Mockito.when(p5.getName()).thenReturn("Place 5");
        Mockito.when(p5.getId()).thenReturn("place_5");
        Mockito.when(p5.getTeamId()).thenReturn("team_id1");
        Mockito.when(p5.getTripStartPlaceAppearances()).thenReturn(0);
        Mockito.when(p5.getTripEndPlaceAppearances()).thenReturn(2);
        Mockito.when(p5.getRideStartPlaceAppearances()).thenReturn(8);
        Mockito.when(p5.getRideEndPlaceAppearances()).thenReturn(9);
        Mockito.when(p5.getLastTripStartPlaceAppearance()).thenReturn(LocalDate.of(2021, 1, 5));
        Mockito.when(p5.getLastTripEndPlaceAppearance()).thenReturn(LocalDate.of(2022, 2, 5));
        Mockito.when(p5.getLastRideStartPlaceAppearance()).thenReturn(LocalDate.of(2021, 3, 5));
        Mockito.when(p5.getLastRideEndPlaceAppearance()).thenReturn(LocalDate.of(2022, 4, 5));


        List<PlaceAppearanceProjection> places = Arrays.asList(p1, p2, p3, p4, p5);

        places.sort(PlaceSorter.of(PlaceSorterOption.RIDE_START));
        assertEquals(places.get(0), p4);
        assertEquals(places.get(4), p3);

        places.sort(PlaceSorter.of(PlaceSorterOption.TRIP_START));
        assertEquals(places.get(0), p4);
        assertEquals(places.get(4), p3);

        places.sort(PlaceSorter.of(PlaceSorterOption.RIDE_END));
        assertEquals(places.get(0), p2);
        assertEquals(places.get(4), p3);

        places.sort(PlaceSorter.of(PlaceSorterOption.TRIP_END));
        assertEquals(places.get(0), p4);
        assertEquals(places.get(4), p3);

        places.sort(PlaceSorter.of(PlaceSorterOption.NAME));
        assertEquals(places.get(0), p1);
        assertEquals(places.get(4), p5);


    }
}
