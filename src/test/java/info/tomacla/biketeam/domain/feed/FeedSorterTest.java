package info.tomacla.biketeam.domain.feed;

import info.tomacla.biketeam.common.data.Timezone;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FeedSorterTest {

    @Test
    public void testEmpty() {

        final List<Feed> feed = new ArrayList<>();
        final List<Feed> sorted = feed.stream().sorted(FeedSorter.get(ZoneOffset.UTC)).collect(Collectors.toList());

        assertNotNull(sorted);

    }

    @Test
    public void testWithElements() {


        ZoneId zoneId = ZoneId.of(Timezone.DEFAULT_TIMEZONE);

        final ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        final LocalDate nowLocal = LocalDate.now(zoneId);

        Feed f1 = new Feed();
        f1.setId("f1");
        f1.setPublishedAt(nowZoned.minus(10, ChronoUnit.DAYS));
        f1.setDate(nowLocal.plus(2, ChronoUnit.DAYS));

        Feed f2 = new Feed();
        f2.setId("f2");
        f2.setPublishedAt(nowZoned.minus(9, ChronoUnit.DAYS));
        f2.setDate(nowLocal.plus(1, ChronoUnit.DAYS));

        Feed f3 = new Feed();
        f3.setId("f3");
        f3.setPublishedAt(nowZoned.minus(9, ChronoUnit.DAYS));

        Feed f4 = new Feed();
        f4.setId("f4");
        f4.setPublishedAt(nowZoned.minus(8, ChronoUnit.DAYS));

        Feed f5 = new Feed();
        f5.setId("f5");
        f5.setPublishedAt(nowZoned.minus(1, ChronoUnit.MONTHS));
        f5.setDate(nowLocal.plus(15, ChronoUnit.DAYS));

        Feed f6 = new Feed();
        f6.setId("f6");
        f6.setPublishedAt(nowZoned.minus(1, ChronoUnit.MONTHS).plus(1, ChronoUnit.DAYS));
        f6.setDate(nowLocal.plus(14, ChronoUnit.DAYS));


        Feed f7 = new Feed();
        f7.setId("f7");
        f7.setPublishedAt(nowZoned.minus(10, ChronoUnit.DAYS));
        f7.setDate(nowLocal.minus(1, ChronoUnit.DAYS));

        final List<Feed> feed = List.of(f1, f2, f3, f4, f5, f6, f7);
        final List<Feed> sorted = feed.stream().sorted(FeedSorter.get(zoneId)).collect(Collectors.toList());

        /**
         * 2 F1 : published 10 days ago and event in 2 days
         * 1 F2 : published 9 days ago and event in 1 day
         * 6 F3 : published 9 days ago but not event
         * 5 F4 : published 8 days ago but not event
         * 4 F5 : published 1 month and event in 15 days
         * 3 F6 : published 1 month minus 1 day and event in 14 days
         * 7 F7 : published 10 days ago and event past
         */

        assertEquals(f2, sorted.get(0));
        assertEquals(f1, sorted.get(1));
        assertEquals(f6, sorted.get(2));
        assertEquals(f5, sorted.get(3));
        assertEquals(f4, sorted.get(4));
        assertEquals(f3, sorted.get(5));
        assertEquals(f7, sorted.get(6));

    }

}
