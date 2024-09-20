package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.geo.Point;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SearchMapSpecification implements Specification<Map> {

    private final Boolean deletion;
    private final String permalink;
    private final Set<String> teamIds;
    private final String name;
    private final Double lowerDistance;
    private final Double upperDistance;
    private final MapType type;
    private final Double lowerPositiveElevation;
    private final Double upperPositiveElevation;
    private final List<String> tags;
    private final WindDirection windDirection;
    private final Point center;
    private final Integer distanceToCenter;

    public SearchMapSpecification(Boolean deletion, String permalink, Set<String> teamIds, String name, Double lowerDistance, Double upperDistance,
                                  MapType type, Double lowerPositiveElevation, Double upperPositiveElevation,
                                  List<String> tags, WindDirection windDirection, Point center, Integer distanceToCenter) {
        this.deletion = deletion;
        this.permalink = permalink;
        this.teamIds = teamIds;
        this.name = name;
        this.lowerDistance = lowerDistance;
        this.upperDistance = upperDistance;
        this.lowerPositiveElevation = lowerPositiveElevation;
        this.upperPositiveElevation = upperPositiveElevation;
        this.tags = Objects.requireNonNullElse(tags, new ArrayList<>());
        this.windDirection = windDirection;
        this.type = type;
        this.center = center;
        this.distanceToCenter = distanceToCenter;
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
        if (teamIds != null && !teamIds.isEmpty()) {
            final CriteriaBuilder.In<Object> teamId = criteriaBuilder.in(root.get("teamId"));
            teamIds.forEach(teamId::value);
            predicates.add(teamId);
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
        if (center != null && distanceToCenter != null) {

            double distance = 0.0089982311915998 * (double) distanceToCenter;

            var minLat = center.getLat() - distance;
            var maxLat = center.getLat() + distance;
            var minLng = center.getLng() - distance;
            var maxLng = center.getLng() + distance;

            predicates.add(criteriaBuilder.or(criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startPoint").get("lat"), minLat),
                    criteriaBuilder.lessThanOrEqualTo(root.get("startPoint").get("lat"), maxLat),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startPoint").get("lng"), minLng),
                    criteriaBuilder.lessThanOrEqualTo(root.get("startPoint").get("lng"), maxLng)
            ), criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endPoint").get("lat"), minLat),
                    criteriaBuilder.lessThanOrEqualTo(root.get("endPoint").get("lat"), maxLat),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endPoint").get("lng"), minLng),
                    criteriaBuilder.lessThanOrEqualTo(root.get("endPoint").get("lng"), maxLng)
            )));

        }
        criteriaQuery.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchMapSpecification readyForDeletion() {
        return new SearchMapSpecification(true, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static SearchMapSpecification allInTeam(String teamId) {
        return new SearchMapSpecification(null, null, Set.of(teamId), null, null, null, null, null, null, null, null, null, null);
    }

    public static SearchMapSpecification byPermalink(String permalink) {
        return new SearchMapSpecification(false, permalink, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static SearchMapSpecification byNameInTeam(String teamId, String name) {
        return new SearchMapSpecification(
                false,
                null,
                Set.of(teamId),
                name,
                null,
                null, null, null, null, null, null, null, null
        );
    }

    public static SearchMapSpecification search(String teamId, String name) {
        return new SearchMapSpecification(
                false,
                null,
                Set.of(teamId),
                name,
                null,
                null, null, null, null, null, null, null, null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchMapSpecification that = (SearchMapSpecification) o;
        return Objects.equals(deletion, that.deletion) && Objects.equals(permalink, that.permalink) && Objects.equals(teamIds, that.teamIds) && Objects.equals(name, that.name) && Objects.equals(lowerDistance, that.lowerDistance) && Objects.equals(upperDistance, that.upperDistance) && type == that.type && Objects.equals(lowerPositiveElevation, that.lowerPositiveElevation) && Objects.equals(upperPositiveElevation, that.upperPositiveElevation) && Objects.equals(tags, that.tags) && windDirection == that.windDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletion, permalink, teamIds, name, lowerDistance, upperDistance, type, lowerPositiveElevation, upperPositiveElevation, tags, windDirection);
    }
}
