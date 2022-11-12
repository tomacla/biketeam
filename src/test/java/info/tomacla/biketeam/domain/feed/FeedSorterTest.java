package info.tomacla.biketeam.domain.feed;

import info.tomacla.biketeam.common.data.Timezone;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FeedSorterTest {

    @Test
    public void testEmpty() {

        final List<FeedEntity> feed = new ArrayList<>();
        final List<FeedEntity> sorted = feed.stream().sorted(FeedSorter.get(ZoneOffset.UTC)).collect(Collectors.toList());

        assertNotNull(sorted);

    }

    @Test
    public void testWithElements() {


        ZoneId zoneId = ZoneId.of(Timezone.DEFAULT_TIMEZONE);

        final ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        final LocalDate nowLocal = LocalDate.now(zoneId);

        FeedEntity f1 = Mockito.mock(FeedEntity.class);
        Mockito.when(f1.getPublishedAt()).thenReturn(nowZoned.minus(10, ChronoUnit.DAYS));
        Mockito.when(f1.getDate()).thenReturn(nowLocal.plus(2, ChronoUnit.DAYS));

        FeedEntity f2 = Mockito.mock(FeedEntity.class);
        Mockito.when(f2.getPublishedAt()).thenReturn(nowZoned.minus(9, ChronoUnit.DAYS));
        Mockito.when(f2.getDate()).thenReturn(nowLocal.plus(1, ChronoUnit.DAYS));

        FeedEntity f3 = Mockito.mock(FeedEntity.class);
        Mockito.when(f3.getPublishedAt()).thenReturn(nowZoned.minus(9, ChronoUnit.DAYS));
        Mockito.when(f3.getDate()).thenReturn(null);

        FeedEntity f4 = Mockito.mock(FeedEntity.class);
        Mockito.when(f4.getPublishedAt()).thenReturn(nowZoned.minus(8, ChronoUnit.DAYS));
        Mockito.when(f4.getDate()).thenReturn(null);

        FeedEntity f5 = Mockito.mock(FeedEntity.class);
        Mockito.when(f5.getPublishedAt()).thenReturn(nowZoned.minus(1, ChronoUnit.MONTHS));
        Mockito.when(f5.getDate()).thenReturn(nowLocal.plus(15, ChronoUnit.DAYS));

        FeedEntity f6 = Mockito.mock(FeedEntity.class);
        Mockito.when(f6.getPublishedAt()).thenReturn(nowZoned.minus(1, ChronoUnit.MONTHS).plus(1, ChronoUnit.DAYS));
        Mockito.when(f6.getDate()).thenReturn(nowLocal.plus(14, ChronoUnit.DAYS));

        FeedEntity f7 = Mockito.mock(FeedEntity.class);
        Mockito.when(f7.getPublishedAt()).thenReturn(nowZoned.minus(10, ChronoUnit.DAYS));
        Mockito.when(f7.getDate()).thenReturn(nowLocal.minus(1, ChronoUnit.DAYS));

        final List<FeedEntity> feed = List.of(f1, f2, f3, f4, f5, f6, f7);
        final List<FeedEntity> sorted = feed.stream().sorted(FeedSorter.get(zoneId)).collect(Collectors.toList());

        assertEquals(f2, sorted.get(0));
        assertEquals(f1, sorted.get(1));
        assertEquals(f6, sorted.get(2));
        assertEquals(f5, sorted.get(3));
        assertEquals(f4, sorted.get(4));
        assertEquals(f3, sorted.get(5));
        assertEquals(f7, sorted.get(6));

    }

}
