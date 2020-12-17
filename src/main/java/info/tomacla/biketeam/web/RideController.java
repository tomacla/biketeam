package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.web.forms.SearchRideForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping(value = "/rides")
public class RideController extends AbstractController {

    @Autowired
    private RideRepository rideRepository;

    @GetMapping
    public String getRides(Principal principal, Model model) {

        LocalDate to = LocalDate.now();
        LocalDate from = to.minus(1L, ChronoUnit.MONTHS);

        SearchRideForm form = new SearchRideForm();
        form.setFrom(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setTo(to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        addGlobalValues(principal, model, "Rides");
        model.addAttribute("rides", rideRepository.findByDateBetweenOrderByDateDesc(from, to));
        model.addAttribute("formdata", form);
        return "rides";
    }

    @PostMapping
    public String getRides(Principal principal, Model model,
                           SearchRideForm form) {

        LocalDate to = LocalDate.parse(form.getTo());
        LocalDate from = LocalDate.parse(form.getFrom());

        addGlobalValues(principal, model, "Rides");
        model.addAttribute("rides", rideRepository.findByDateBetweenOrderByDateDesc(from, to));
        model.addAttribute("formdata", form);
        return "rides";
    }

    @GetMapping(value = "/{rideId}")
    public String getRide(@PathVariable("rideId") String rideId,
                          Principal principal, Model model) {
        Ride ride = rideRepository.findById(rideId).get();
        addGlobalValues(principal, model, "Ride " + ride.getTitle());
        model.addAttribute("ride", ride);
        return "ride";
    }

}
