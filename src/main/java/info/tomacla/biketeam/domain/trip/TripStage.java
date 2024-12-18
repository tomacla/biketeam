package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.map.Map;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "trip_stage")
public class TripStage {

    @Id
    @UuidGenerator
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Trip trip;
    @Column(name = "date")
    private LocalDate date;
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id")
    private Map map;
    private boolean alternative;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id is null");
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "date is null");
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public boolean isAlternative() {
        return alternative;
    }

    public void setAlternative(boolean alternative) {
        this.alternative = alternative;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TripStage tripStage = (TripStage) o;
        return alternative == tripStage.alternative && Objects.equals(id, tripStage.id) && Objects.equals(trip, tripStage.trip) && Objects.equals(date, tripStage.date) && Objects.equals(name, tripStage.name) && Objects.equals(map, tripStage.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trip, date, name, map, alternative);
    }
}