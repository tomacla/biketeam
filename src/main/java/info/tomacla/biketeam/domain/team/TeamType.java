package info.tomacla.biketeam.domain.team;

public enum TeamType {

    BIKE(true), HIKE(false);

    private boolean simplifyGpx;

    TeamType(boolean simplifyGpx) {
        this.simplifyGpx = simplifyGpx;
    }

    public boolean isSimplifyGpx() {
        return simplifyGpx;
    }

}
