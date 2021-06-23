package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.Country;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SearchTeamSpecification implements Specification<Team> {

    private final String name;
    private final String city;
    private final Country country;

    public SearchTeamSpecification(String name, String city, Country country) {
        this.name = name;
        this.city = city;
        this.country = country;
    }

    @Override
    public Predicate toPredicate(Root<Team> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (city != null && !city.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
        }
        if (country != null) {
            predicates.add(criteriaBuilder.like(root.get("country"), country.name()));
        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
