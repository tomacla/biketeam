package info.tomacla.biketeam.domain.map;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchMapSpecification implements Specification<Map> {

    private double lowerDistance;
    private double upperDistance;
    private List<String> tags;
    private WindDirection windDirection;

    public SearchMapSpecification(double lowerDistance, double upperDistance, List<String> tags, WindDirection windDirection) {
        this.lowerDistance = lowerDistance;
        this.upperDistance = upperDistance;
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
        this.windDirection = windDirection;
    }

    @Override
    public Predicate toPredicate(Root<Map> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("length"), lowerDistance));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("length"), upperDistance));
        if (!tags.isEmpty()) {
            predicates.add(root.join("tags").in(tags));
        }
        if (windDirection != null) {
            predicates.add(criteriaBuilder.equal(root.get("windDirection"), windDirection));
        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
