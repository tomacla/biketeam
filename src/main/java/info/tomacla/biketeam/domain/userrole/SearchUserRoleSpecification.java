package info.tomacla.biketeam.domain.userrole;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchUserRoleSpecification implements Specification<UserRole> {

    private final String teamId;

    public SearchUserRoleSpecification(String teamId) {
        this.teamId = teamId;
    }

    @Override
    public Predicate toPredicate(Root<UserRole> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (teamId != null && !teamId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchUserRoleSpecification that = (SearchUserRoleSpecification) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}
