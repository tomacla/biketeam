package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.Country;
import info.tomacla.biketeam.common.Strings;

import java.util.Objects;

public class SearchTeamForm {

    private int page;
    private int pageSize;
    private String name;
    private String city;
    private String country;

    public SearchTeamForm() {
        setPage(0);
        setPageSize(9);
        setName(null);
        setCountry(null);
        setCity(null);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = Objects.requireNonNullElse(city, "");
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = Objects.requireNonNullElse(country, "");
    }

    public SearchTeamFormParser parser() {
        return new SearchTeamFormParser(this);
    }

    public static SearchTeamFormBuilder builder() {
        return new SearchTeamFormBuilder();
    }

    public static class SearchTeamFormParser {

        private final SearchTeamForm form;

        protected SearchTeamFormParser(SearchTeamForm form) {
            this.form = form;
        }

        public int getPage() {
            return form.getPage();
        }

        public int getPageSize() {
            return form.getPageSize();
        }

        public String getName() {
            return Strings.requireNonBlankOrNull(form.getName());
        }

        public String getCity() {
            return Strings.requireNonBlankOrNull(form.getCity());
        }

        public Country getCountry() {
            if (form.getCountry() != null && !form.getCountry().isBlank()) {
                return Country.valueOf(form.getCountry());
            }
            return null;
        }

    }

    public static class SearchTeamFormBuilder {

        private final SearchTeamForm form;

        protected SearchTeamFormBuilder() {
            this.form = new SearchTeamForm();
        }

        public SearchTeamFormBuilder withPage(int page) {
            form.setPage(page);
            return this;
        }

        public SearchTeamFormBuilder withPageSize(int pageSize) {
            form.setPageSize(pageSize);
            return this;
        }

        public SearchTeamFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }

        public SearchTeamFormBuilder withCountry(Country country) {
            if (country != null) {
                form.setCountry(country.name());
            }
            return this;
        }

        public SearchTeamFormBuilder withCity(String city) {
            form.setCity(city);
            return this;
        }

        public SearchTeamForm get() {
            return form;
        }

    }

}
