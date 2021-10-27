package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.Strings;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "trip_stage")
public class TripStage {

    @Id
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Trip trip;
    @Column(name = "date")
    private LocalDate date;
    private String name;
    @Column(name = "map_id")
    private String mapId;

    protected TripStage() {

    }

    public TripStage(String name,
                     LocalDate date,
                     String mapId) {
        setName(name);
        setDate(date);
        setMapId(mapId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
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

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = Strings.requireNonBlankOrNull(mapId);
    }

    public void setTrip(Trip trip, int index) {
        this.trip = Objects.requireNonNull(trip);
        this.id = trip.getId() + "-" + index;
    }

    public int getStageIndex() {
        final String[] parts = this.id.split("-");
        return Integer.valueOf(parts[parts.length - 1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripStage tripStage = (TripStage) o;
        return id.equals(tripStage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void removeTrip() {
        this.trip = null;
    }

}