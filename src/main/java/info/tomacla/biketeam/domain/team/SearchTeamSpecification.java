package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.data.Country;
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
    private final List<Visibility> visibilities;

    public SearchTeamSpecification(String name, String city, Country country, List<Visibility> visibilities) {
        this.name = name;
        this.city = city;
        this.country = country;
        this.visibilities = visibilities;
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
            predicates.add(criteriaBuilder.equal(root.get("country"), country));
        }
        if (!visibilities.isEmpty()) {
            final CriteriaBuilder.In<Object> visibility = criteriaBuilder.in(root.get("visibility"));
            visibilities.forEach(visibility::value);
            predicates.add(visibility);
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
