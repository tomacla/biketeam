package info.tomacla.biketeam.service.pedalons;

/**
 * Compteurs de ce qui sera migre (elements non supprimes), repris dans le claim summary du jeton
 * de demande et affiches sur la page d'administration.
 */
public class PedalonsSummary {

    private final long places;
    private final long routes;
    private final long rides;
    private final long rideTemplates;
    private final long trips;
    private final long tripStages;
    private final long publications;
    private final boolean faqPage;
    private final boolean logo;

    public PedalonsSummary(long places, long routes, long rides, long rideTemplates, long trips, long tripStages,
                           long publications, boolean faqPage, boolean logo) {
        this.places = places;
        this.routes = routes;
        this.rides = rides;
        this.rideTemplates = rideTemplates;
        this.trips = trips;
        this.tripStages = tripStages;
        this.publications = publications;
        this.faqPage = faqPage;
        this.logo = logo;
    }

    public long getPlaces() {
        return places;
    }

    public long getRoutes() {
        return routes;
    }

    public long getRides() {
        return rides;
    }

    public long getRideTemplates() {
        return rideTemplates;
    }

    public long getTrips() {
        return trips;
    }

    public long getTripStages() {
        return tripStages;
    }

    public long getPublications() {
        return publications;
    }

    public boolean isFaqPage() {
        return faqPage;
    }

    public boolean isLogo() {
        return logo;
    }
}
