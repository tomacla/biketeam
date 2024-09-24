package info.tomacla.biketeam.domain.template;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SearchRideTemplateSpecification implements Specification<RideTemplate> {
    private final String teamId;

    public SearchRideTemplateSpecification(String teamId) {
        this.teamId = teamId;
    }

    @Override
    public Predicate toPredicate(Root<RideTemplate> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();
        if (teamId != null && !teamId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchRideTemplateSpecification allInTeam(String teamId) {
        return new SearchRideTemplateSpecification(teamId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRideTemplateSpecification that = (SearchRideTemplateSpecification) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}
