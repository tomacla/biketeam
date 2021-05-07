package info.tomacla.biketeam.web.forms;

import com.fasterxml.jackson.core.type.TypeReference;
import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.WindDirection;

import java.util.List;

public class SearchMapForm {

    private int page;
    private int pageSize;
    private double lowerDistance;
    private double upperDistance;
    private String sort;
    private String windDirection;
    private String tags;

    public SearchMapForm() {
        setPage(0);
        setPageSize(9);
        setLowerDistance(1);
        setUpperDistance(1000);
        setTags("[]");
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
        this.sort = sort;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public static SearchMapFormParser parser(SearchMapForm form) {
        return new SearchMapFormParser(form);
    }

    public static SearchMapFormBuilder builder() {
        return new SearchMapFormBuilder();
    }

    public static class SearchMapFormParser {

        private SearchMapForm form;

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
            return Json.parse(form.getTags(), new TypeReference<List<String>>() {
            });
        }

    }

    public static class SearchMapFormBuilder {

        private SearchMapForm form;

        protected SearchMapFormBuilder() {
            this.form = new SearchMapForm();
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
            form.setSort(sort.name().toLowerCase());
            return this;
        }

        public SearchMapFormBuilder withWindDirection(WindDirection windDirection) {
            form.setWindDirection(windDirection.name());
            return this;
        }

        public SearchMapFormBuilder withTags(List<String> tags) {
            form.setTags(Json.serialize(tags));
            return this;
        }

        public SearchMapForm get() {
            return form;
        }

    }

}
