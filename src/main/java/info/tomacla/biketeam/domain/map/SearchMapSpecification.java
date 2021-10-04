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

    private final String teamId;
    private final double lowerDistance;
    private final double upperDistance;
    private final MapType type;
    private final double lowerPositiveElevation;
    private final double upperPositiveElevation;
    private final List<String> tags;
    private final WindDirection windDirection;
    private Boolean visible;

    public SearchMapSpecification(String teamId, double lowerDistance, double upperDistance,
                                  MapType type, double lowerPositiveElevation, double upperPositiveElevation,
                                  List<String> tags, WindDirection windDirection, Boolean visible) {
        this.teamId = teamId;
        this.lowerDistance = lowerDistance;
        this.upperDistance = upperDistance;
        this.lowerPositiveElevation = lowerPositiveElevation;
        this.upperPositiveElevation = upperPositiveElevation;
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
        this.windDirection = windDirection;
        this.type = type;
        this.visible = visible;
    }

    @Override
    public Predicate toPredicate(Root<Map> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("length"), lowerDistance));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("length"), upperDistance));
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("positiveElevation"), lowerPositiveElevation));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("positiveElevation"), upperPositiveElevation));
        if (type != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), type));
        }
        if (!tags.isEmpty()) {
            predicates.add(root.join("tags").in(tags));
        }
        if (windDirection != null) {
            predicates.add(criteriaBuilder.equal(root.get("windDirection"), windDirection));
        }
        if(visible != null) {
            predicates.add(criteriaBuilder.equal(root.get("visible"), visible));
        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
