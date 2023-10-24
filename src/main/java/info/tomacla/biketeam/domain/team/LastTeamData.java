package info.tomacla.biketeam.domain.team;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public interface LastTeamData {

    String getId();

    LocalDate getLastRidePublishedAt();

    LocalDate getLastTripPublishedAt();

    LocalDate getLastPublicationPublishedAt();

    public static LocalDate extractDate(List<LastTeamData> dataset, Function<LastTeamData, LocalDate> supplier,
                                        LocalDate minimalOrDefault) {
        LocalDate selected = dataset.stream()
                .filter(d -> supplier.apply(d) != null)
                .sorted(Comparator.comparing(supplier).reversed())
                .map(supplier)
                .findFirst().orElse(minimalOrDefault);

        return selected.isAfter(minimalOrDefault) ? minimalOrDefault : selected;

    }

}
