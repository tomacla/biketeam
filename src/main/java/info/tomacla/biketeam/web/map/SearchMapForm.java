package info.tomacla.biketeam.web.map;

import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.WindDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchMapForm {

    private int page;
    private int pageSize;
    private double lowerDistance;
    private double upperDistance;
    private String sort;
    private String windDirection;
    private List<String> tags;

    public SearchMapForm() {
        setPage(0);
        setPageSize(9);
        setLowerDistance(1);
        setUpperDistance(1000);
        setTags(new ArrayList<>());
        setWindDirection("");
        setSort("");
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

    public double getLowerDistance() {
        return lowerDistance;
    }

    public void setLowerDistance(double lowerDistance) {
        this.lowerDistance = lowerDistance;
    }

    public double getUpperDistance() {
        return upperDistance;
    }

    public void setUpperDistance(double upperDistance) {
        this.upperDistance = upperDistance;
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

    public static SearchMapFormBuilder builder() {
        return new SearchMapFormBuilder();
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

        public double getLowerDistance() {
            return form.getLowerDistance();
        }

        public double getUpperDistance() {
            return form.getUpperDistance();
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

        public SearchMapFormBuilder withLowerDistance(double lowerDistance) {
            form.setLowerDistance(lowerDistance);
            return this;
        }

        public SearchMapFormBuilder withUpperDistance(double upperDistance) {
            form.setUpperDistance(upperDistance);
            return this;
        }

        public SearchMapFormBuilder withSort(MapSorterOption sort) {
            if (sort != null) {
                form.setSort(sort.name().toLowerCase());
            }
            return this;
        }

        public SearchMapFormBuilder withWindDirection(WindDirection windDirection) {
            if (windDirection != null) {
                form.setWindDirection(windDirection.name());
            }
            return this;
        }

        public SearchMapFormBuilder withTags(List<String> tags) {
            if (tags != null) {
                form.setTags(tags);
            }
            return this;
        }

        public SearchMapForm get() {
            return form;
        }

    }

}
