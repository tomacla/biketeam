package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.TeamDTO;
import info.tomacla.biketeam.common.data.Country;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class DataAPI {

    @GetMapping(path = "/countries", produces = "application/json")
    public List<Map<String, String>> getCountries() {

        return Arrays.stream(Country.values()).map(c -> Map.of("code", c.name(), "label", c.getLabel())).collect(Collectors.toList());

    }

}
