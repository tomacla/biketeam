package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchMapForm {

    private int page = 0;
    private int pageSize = 18;
    private String lowerDistance = "";
    private String upperDistance = "";
    private String lowerPositiveElevation = "";
    private String upperPositiveElevation = "";
    private String sort = "";
    private String type = "";
    private String windDirection = "";
    private String name = "";
    private List<String> tags = new ArrayList<>();

    private String centerAddress = "";
    private String centerAddressLat = "";
    private String centerAddressLng = "";
    private String distanceToCenter = "10";

    public static SearchMapFormBuilder builder() {
        return new SearchMapFormBuilder();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getLowerDistance() {
        return lowerDistance;
    }

    public void setLowerDistance(String lowerDistance) {
        this.lowerDistance = Objects.requireNonNullElse(lowerDistance, "");
    }

    public String getUpperDistance() {
        return upperDistance;
    }

    public void setUpperDistance(String upperDistance) {
        this.upperDistance = Objects.requireNonNullElse(upperDistance, "");
    }

    public String getLowerPositiveElevation() {
        return lowerPositiveElevation;
    }

    public void setLowerPositiveElevation(String lowerPositiveElevation) {
        this.lowerPositiveElevation = Objects.requireNonNullElse(lowerPositiveElevation, "");
    }

    public String getUpperPositiveElevation() {
        return upperPositiveElevation;
    }

    public void setUpperPositiveElevation(String upperPositiveElevation) {
        this.upperPositiveElevation = Objects.requireNonNullElse(upperPositiveElevation, "");
    }

    public String getCenterAddress() {
        return centerAddress;
    }

    public void setCenterAddress(String centerAddress) {
        this.centerAddress = Objects.requireNonNullElse(centerAddress, "");
    }

    public String getCenterAddressLat() {
        return centerAddressLat;
    }

    public void setCenterAddressLat(String centerAddressLat) {
        this.centerAddressLat = Objects.requireNonNullElse(centerAddressLat, "");
    }

    public String getCenterAddressLng() {
        return centerAddressLng;
    }

    public void setCenterAddressLng(String centerAddressLng) {
        this.centerAddressLng = Objects.requireNonNullElse(centerAddressLng, "");
    }

    public String getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(String distanceToCenter) {
        this.distanceToCenter = Objects.requireNonNullElse(distanceToCenter, "10");
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = Objects.requireNonNullElse(type, "");
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = Objects.requireNonNullElse(sort, "");
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = Objects.requireNonNullElse(windDirection, "");
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
    }

    public SearchMapFormParser parser() {
        return new SearchMapFormParser(this);
    }

    public static class SearchMapFormParser {

        private final SearchMapForm form;

        protected SearchMapFormParser(SearchMapForm form) {
            this.form = form;
        }

        public int getPage() {
            return form.getPage();
        }

        public int getPageSize() {
            return form.getPageSize();
        }

        public Double getLowerDistance() {
            if (form.getLowerDistance() == null || form.getLowerDistance().isBlank()) {
                return null;
            }
            return Double.parseDouble(form.getLowerDistance());
        }

        public Double getUpperDistance() {
            if (form.getUpperDistance() == null || form.getUpperDistance().isBlank()) {
                return null;
            }
            return Double.parseDouble(form.getUpperDistance());
        }

        public Double getLowerPositiveElevation() {
            if (form.getLowerPositiveElevation() == null || form.getLowerPositiveElevation().isBlank()) {
                return null;
            }
            return Double.parseDouble(form.getLowerPositiveElevation());
        }

        public Double getUpperPositiveElevation() {
            if (form.getUpperPositiveElevation() == null || form.getUpperPositiveElevation().isBlank()) {
                return null;
            }
            return Double.parseDouble(form.getUpperPositiveElevation());
        }

        public String getCenterAddress() {
            if (form.getCenterAddress() == null || form.getCenterAddress().isBlank()) {
                return null;
            }
            return form.getCenterAddress();
        }

        public Point getCenterAddressPoint() {
            if (form.getCenterAddressLat() == null || form.getCenterAddressLat().isBlank()
            || form.getCenterAddressLng() == null || form.getCenterAddressLng().isBlank() ) {
                return null;
            }
            return new Point(Double.parseDouble(form.getCenterAddressLat()), Double.parseDouble(form.getCenterAddressLng()));
        }

        public Integer getDistanceToCenter() {
            if (form.getDistanceToCenter() == null || form.getDistanceToCenter().isBlank()) {
                return null;
            }
            return Integer.parseInt(form.getDistanceToCenter());
        }

        public MapSorterOption getSort() {
            if (form.getSort() == null || form.getSort().isBlank()) {
                return null;
            }
            return MapSorterOption.valueOf(form.getSort().toUpperCase());
        }

        public WindDirection getWindDirection() {
            if (form.getWindDirection() == null || form.getWindDirection().isBlank()) {
                return null;
            }
            return WindDirection.valueOf(form.getWindDirection());
        }

        public MapType getType() {
            if (form.getType() == null || form.getType().isBlank()) {
                return null;
            }
            return MapType.valueOf(form.getType());
        }

        public String getName() {
            if (form.getName() == null || form.getName().isBlank()) {
                return null;
            }
            return form.getName();
        }

        public List<String> getTags() {
            return form.getTags();
        }

    }

    public static class SearchMapFormBuilder {

        private final SearchMapForm form;

        protected SearchMapFormBuilder() {
            this.form = new SearchMapForm();
        }

        public SearchMapFormBuilder withPage(int page) {
            form.setPage(page);
            return this;
        }

        public SearchMapFormBuilder withPageSize(int pageSize) {
            form.setPageSize(pageSize);
            return this;
        }

        public SearchMapFormBuilder withLowerDistance(Double lowerDistance) {
            if (lowerDistance != null) {
                form.setLowerDistance(String.valueOf(lowerDistance.intValue()));
            }
            return this;
        }

        public SearchMapFormBuilder withUpperDistance(Double upperDistance) {
            if (upperDistance != null) {
                form.setUpperDistance(String.valueOf(upperDistance.intValue()));
            }
            return this;
        }

        public SearchMapFormBuilder withLowerPositiveElevation(Double positiveElevation) {
            if (positiveElevation != null) {
                form.setLowerPositiveElevation(String.valueOf(positiveElevation.intValue()));
            }
            return this;
        }

        public SearchMapFormBuilder withUpperPositiveElevation(Double positiveElevation) {
            if (positiveElevation != null) {
                form.setUpperPositiveElevation(String.valueOf(positiveElevation.intValue()));
            }
            return this;
        }


        public SearchMapFormBuilder withCenterAddress(String centerAddress) {
            form.setCenterAddress(centerAddress);
            return this;
        }


        public SearchMapFormBuilder withCenterAddressPoint(Point centerAddressPoint) {
            if(centerAddressPoint != null) {
                form.setCenterAddressLat(""+centerAddressPoint.getLat());
                form.setCenterAddressLng(""+centerAddressPoint.getLng());
            }
            return this;
        }


        public SearchMapFormBuilder withDistanceToCenter(Integer distanceToCenter) {
            if(distanceToCenter != null) {
                form.setDistanceToCenter(""+distanceToCenter);
            }
            return this;
        }

        public SearchMapFormBuilder withSort(MapSorterOption sort) {
            if (sort != null) {
                form.setSort(sort.name());
            }
            return this;
        }

        public SearchMapFormBuilder withWindDirection(WindDirection windDirection) {
            if (windDirection != null) {
                form.setWindDirection(windDirection.name());
            }
            return this;
        }

        public SearchMapFormBuilder withType(MapType mapType) {
            if (mapType != null) {
                form.setType(mapType.name());
            }
            return this;
        }

        public SearchMapFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public SearchMapFormBuilder withTags(List<String> tags) {
            if (tags != null && !tags.isEmpty()) {
                form.setTags(tags);
            }
            return this;
        }

        public SearchMapForm get() {
            return form;
        }

    }

}
