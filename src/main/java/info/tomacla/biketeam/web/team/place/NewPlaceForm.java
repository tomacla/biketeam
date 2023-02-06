package info.tomacla.biketeam.web.team.place;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.geo.Point;

public class NewPlaceForm {

    private String id = "new";
    private String name = "";
    private String address = "";
    private String link = "";
    private double pointLat = 0.0;
    private double pointLng = 0.0;

    private String startPlace = null;

    private String endPlace = null;

    public static NewPlaceFormBuilder builder() {
        return new NewPlaceFormBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlankOrDefault(id, "new");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlankOrDefault(name, "");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = Strings.requireNonBlankOrDefault(address, "");
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = Strings.requireNonBlankOrDefault(link, "");
    }

    public double getPointLat() {
        return pointLat;
    }

    public void setPointLat(double pointLat) {
        this.pointLat = pointLat;
    }

    public double getPointLng() {
        return pointLng;
    }

    public void setPointLng(double pointLng) {
        this.pointLng = pointLng;
    }

    public String getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(String startPlace) {
        this.startPlace = startPlace;
    }

    public String getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(String endPlace) {
        this.endPlace = endPlace;
    }

    public NewPlaceFormParser parser() {
        return new NewPlaceFormParser(this);
    }

    public static class NewPlaceFormParser {

        private final NewPlaceForm form;

        public NewPlaceFormParser(NewPlaceForm form) {
            this.form = form;
        }

        public String getId() {
            return form.getId();
        }

        public String getName() {
            return Strings.requireNonBlankOrNull(form.getName());
        }

        public String getAddress() {
            return Strings.requireNonBlankOrNull(form.getAddress());
        }

        public String getLink() {
            String target = form.getLink();
            if (Strings.isBlank(target)) {
                return null;
            }

            if (!target.toLowerCase().startsWith("http")) {
                target = "http://" + target;
            }

            return target;
        }

        public Point getPoint() {
            if (form.getPointLat() != 0.0 && form.getPointLng() != 0.0) {
                return new Point(form.getPointLat(), form.getPointLng());
            }
            return null;
        }

        public boolean isStartPlace() {
            return form.getStartPlace() != null && form.getStartPlace().equals("on");
        }

        public boolean isEndPlace() {
            return form.getEndPlace() != null && form.getEndPlace().equals("on");
        }

    }

    public static class NewPlaceFormBuilder {

        private final NewPlaceForm form;

        public NewPlaceFormBuilder() {
            this.form = new NewPlaceForm();
        }

        public NewPlaceFormBuilder withId(String id) {
            form.setId(id);
            return this;
        }

        public NewPlaceFormBuilder withName(String name) {
            form.setName(name);
            return this;
        }


        public NewPlaceFormBuilder withAddress(String address) {
            form.setAddress(address);
            return this;
        }

        public NewPlaceFormBuilder withLink(String link) {
            form.setLink(link);
            return this;
        }

        public NewPlaceFormBuilder withPoint(Point point) {
            if (point != null) {
                form.setPointLat(point.getLat());
                form.setPointLng(point.getLng());
            }
            return this;
        }

        public NewPlaceFormBuilder withStartPlace(boolean startPlace) {
            form.setStartPlace(startPlace ? "on" : null);
            return this;
        }


        public NewPlaceFormBuilder withEndPlace(boolean endPlace) {
            form.setEndPlace(endPlace ? "on" : null);
            return this;
        }

        public NewPlaceForm get() {
            return form;
        }


    }

}
