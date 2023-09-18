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

    private final Boolean deletion;
    private final String permalink;
    private final String teamId;
    private final String name;
    private final Double lowerDistance;
    private final Double upperDistance;
    private final MapType type;
    private final Double lowerPositiveElevation;
    private final Double upperPositiveElevation;
    private final List<String> tags;
    private final WindDirection windDirection;

    public SearchMapSpecification(Boolean deletion, String permalink, String teamId, String name, Double lowerDistance, Double upperDistance,
                                  MapType type, Double lowerPositiveElevation, Double upperPositiveElevation,
                                  List<String> tags, WindDirection windDirection) {
        this.deletion = deletion;
        this.permalink = permalink;
        this.teamId = teamId;
        this.name = name;
        this.lowerDistance = lowerDistance;
        this.upperDistance = upperDistance;
        this.lowerPositiveElevation = lowerPositiveElevation;
        this.upperPositiveElevation = upperPositiveElevation;
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
        this.windDirection = windDirection;
        this.type = type;
    }

    @Override
    public Predicate toPredicate(Root<Map> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();
        if (deletion != null) {
            predicates.add(criteriaBuilder.equal(root.get("deletion"), deletion.booleanValue()));
        }
        if (permalink != null && !permalink.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("permalink"), permalink));
        }
        if (teamId != null && !teamId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
        }
        if (lowerDistance != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("length"), lowerDistance));
        }
        if (upperDistance != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("length"), upperDistance));
        }
        if (lowerPositiveElevation != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("positiveElevation"), lowerPositiveElevation));
        }
        if (upperPositiveElevation != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("positiveElevation"), upperPositiveElevation));
        }
        if (name != null && !name.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (type != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), type));
        }
        if (tags != null && !tags.isEmpty()) {
            predicates.add(root.join("tags").in(tags));
        }
        if (windDirection != null) {
            predicates.add(criteriaBuilder.equal(root.get("windDirection"), windDirection));
        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchMapSpecification readyForDeletion() {
        return new SearchMapSpecification(true, null, null, null, null, null, null, null, null, null, null);
    }

}
