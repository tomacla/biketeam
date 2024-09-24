package info.tomacla.biketeam.domain.team;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public interface LastTeamData {

    String getId();

    Instant getLastRidePublishedAt();

    Instant getLastTripPublishedAt();

    Instant getLastPublicationPublishedAt();

    public static LocalDate extractDate(List<LastTeamData> dataset, Function<LastTeamData, Instant> supplier,
                                        Instant minimalOrDefault) {
        Instant selected = dataset.stream()
                .filter(d -> supplier.apply(d) != null)
                .sorted(Comparator.comparing(supplier).reversed())
                .map(supplier)
                .findFirst().orElse(minimalOrDefault);

        Instant result = selected.isAfter(minimalOrDefault) ? minimalOrDefault : selected;
        return result.atOffset(ZoneOffset.UTC).toLocalDate();

    }

}
