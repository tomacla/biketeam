package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.domain.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SearchTeamSpecification implements Specification<Team> {

    private final Boolean deletion;
    private final String name;
    private final String city;
    private final Country country;
    private final List<Visibility> visibilities;
    private final User user;

    public SearchTeamSpecification(Boolean deletion, String name, String city, Country country, List<Visibility> visibilities, User user) {
        this.deletion = deletion;
        this.name = name;
        this.city = city;
        this.country = country;
        this.visibilities = visibilities;
        this.user = user;
    }

    @Override
    public Predicate toPredicate(Root<Team> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (deletion != null) {
            predicates.add(criteriaBuilder.equal(root.get("deletion"), deletion.booleanValue()));
        }
        if (name != null && !name.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (city != null && !city.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
        }
        if (country != null) {
            predicates.add(criteriaBuilder.equal(root.get("country"), country));
        }
        if (visibilities != null && !visibilities.isEmpty()) {
            final CriteriaBuilder.In<Object> visibility = criteriaBuilder.in(root.get("visibility"));
            visibilities.forEach(visibility::value);
            predicates.add(visibility);
        }
        if (user != null) {
            predicates.add(criteriaBuilder.equal(root.join("roles").get("user"), user));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchTeamSpecification readyForDeletion() {
        return new SearchTeamSpecification(true, null, null, null, null, null);
    }

    public static SearchTeamSpecification publiclyVisible() {
        return new SearchTeamSpecification(false, null, null, null, List.of(Visibility.PUBLIC), null);
    }

    public static SearchTeamSpecification ofUser(User user) {
        return new SearchTeamSpecification(false, null, null, null, null, user);
    }

    public static SearchTeamSpecification search(String name) {
        return new SearchTeamSpecification(false, name, null, null, List.of(Visibility.PUBLIC, Visibility.PRIVATE), null);
    }

    public static SearchTeamSpecification all() {
        return new SearchTeamSpecification(false, null, null, null, null, null);
    }

}
