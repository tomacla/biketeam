package info.tomacla.biketeam.domain.feed;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapSorter;
import info.tomacla.biketeam.domain.map.MapSorterOption;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;

public class FeedSorter {

    public static Comparator<Feed> get(ZoneId zoneId) {
        return new FeedSorter.FeedComparator(zoneId);
    }

    public static class FeedComparator implements Comparator<Feed> {

        private ZoneId zoneId;

        public FeedComparator(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        @Override
        public int compare(Feed f1, Feed f2) {

            boolean f1Passed = f1.getDate() == null || f1.getDate().isBefore(LocalDate.now(zoneId));
            boolean f2Passed = f2.getDate() == null || f2.getDate().isBefore(LocalDate.now(zoneId));

            if(!f1Passed && f2Passed) {
                return -1;
            }
            if(f1Passed && !f2Passed) {
                return 1;
            }
            if(f1Passed && f2Passed) {
                return f2.getPublishedAt().compareTo(f1.getPublishedAt());
            }

            return f1.getDate().compareTo(f2.getDate());

        }
    }

}
