package info.tomacla.biketeam.domain.place;


import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchPlaceSpecification implements Specification<Place> {

    private final String teamId;
    private final Boolean startPlace;
    private final Boolean endPlace;

    public SearchPlaceSpecification(String teamId, Boolean startPlace, Boolean endPlace) {
        this.teamId = teamId;
        this.startPlace = startPlace;
        this.endPlace = endPlace;
    }

    @Override
    public Predicate toPredicate(Root<Place> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (teamId != null && !teamId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
        }
        if (startPlace != null) {
            predicates.add(criteriaBuilder.equal(root.get("startPlace"), startPlace));
        }
        if (endPlace != null) {
            predicates.add(criteriaBuilder.equal(root.get("endPlace"), endPlace));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchPlaceSpecification allInTeam(String teamId) {
        return new SearchPlaceSpecification(teamId, null, null);
    }

    public static SearchPlaceSpecification startPlaceInTeam(String teamId) {
        return new SearchPlaceSpecification(teamId, true, null);
    }

    public static SearchPlaceSpecification endPlaceInTeam(String teamId) {
        return new SearchPlaceSpecification(teamId, null, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchPlaceSpecification that = (SearchPlaceSpecification) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(startPlace, that.startPlace) && Objects.equals(endPlace, that.endPlace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, startPlace, endPlace);
    }
}
