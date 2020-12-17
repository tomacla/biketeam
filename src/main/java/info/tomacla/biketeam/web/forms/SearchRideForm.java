package info.tomacla.biketeam.web.forms;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SearchRideForm {

    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public static SearchRideForm empty() {
        SearchRideForm form = new SearchRideForm();
        form.setTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setFrom(LocalDate.now().minus(1, ChronoUnit.MONTHS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        return form;
    }

}
