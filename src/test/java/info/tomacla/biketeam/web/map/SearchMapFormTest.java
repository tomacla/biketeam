package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SearchMapFormTest {

    @Test
    public void test() {

        final SearchMapForm searchMapForm = SearchMapForm.builder()
                .withLowerDistance(10)
                .withUpperDistance(100)
                .withPage(5)
                .withPageSize(10)
                .withSort(MapSorterOption.HILLY)
                .withTags(List.of("toot", "taat"))
                .withType(MapType.ROAD)
                .withName("foo")
                .withLowerPositiveElevation(100)
                .withUpperPositiveElevation(200)
                .withWindDirection(WindDirection.NORTH)
                .get();

        final SearchMapForm.SearchMapFormParser parser = searchMapForm.parser();

        assertEquals(10, parser.getLowerDistance());
        assertEquals(100, parser.getUpperDistance());
        assertEquals(5, parser.getPage());
        assertEquals(10, parser.getPageSize());
        assertEquals(MapSorterOption.HILLY, parser.getSort());
        assertEquals(List.of("toot", "taat"), parser.getTags());
        assertEquals(MapType.ROAD, parser.getType());
        assertEquals("foo", parser.getName());
        assertEquals(100, parser.getLowerPositiveElevation());
        assertEquals(200, parser.getUpperPositiveElevation());
        assertEquals(WindDirection.NORTH, parser.getWindDirection());

    }

    @Test
    public void testDefault() {

        final SearchMapForm searchMapForm = SearchMapForm.builder().get();

        final SearchMapForm.SearchMapFormParser parser = searchMapForm.parser();

        assertEquals(1, parser.getLowerDistance());
        assertEquals(1000, parser.getUpperDistance());
        assertEquals(0, parser.getPage());
        assertEquals(9, parser.getPageSize());
        assertNull(parser.getSort());
        assertEquals(0, parser.getTags().size());
        assertNull(parser.getType());
        assertNull(parser.getName());
        assertEquals(0, parser.getLowerPositiveElevation());
        assertEquals(3000, parser.getUpperPositiveElevation());
        assertNull(parser.getWindDirection());

    }

}
