package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class SearchPublicationSpecification implements Specification<Publication> {

    private final Boolean deletion;
    private final Set<String> teamIds;
    private final String title;
    private final PublishedStatus publishedStatus;
    private final ZonedDateTime minPublishedDate;
    private final ZonedDateTime maxPublishedDate;

    public SearchPublicationSpecification(Boolean deletion, Set<String> teamIds, String title, PublishedStatus publishedStatus, ZonedDateTime minPublishedDate, ZonedDateTime maxPublishedDate) {
        this.deletion = deletion;
        this.teamIds = teamIds;
        this.title = title;
        this.publishedStatus = publishedStatus;
        this.minPublishedDate = minPublishedDate;
        this.maxPublishedDate = maxPublishedDate;
    }

    @Override
    public Predicate toPredicate(Root<Publication> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();
        if (deletion != null) {
            predicates.add(criteriaBuilder.equal(root.get("deletion"), deletion.booleanValue()));
        }
        if (teamIds != null && !teamIds.isEmpty()) {
            final CriteriaBuilder.In<Object> teamId = criteriaBuilder.in(root.get("teamId"));
            teamIds.forEach(teamId::value);
            predicates.add(teamId);
        }
        if (title != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (publishedStatus != null) {
            predicates.add(criteriaBuilder.equal(root.get("publishedStatus"), publishedStatus));
        }
        if (minPublishedDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishedAt"), minPublishedDate));
        }
        if (maxPublishedDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("publishedAt"), maxPublishedDate));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchPublicationSpecification readyForDeletion() {
        return new SearchPublicationSpecification(true, null, null, null, null, null);
    }

    public static SearchPublicationSpecification allInTeam(String teamId) {
        return new SearchPublicationSpecification(null, Set.of(teamId), null, null, null, null);
    }

    public static SearchPublicationSpecification byTitleInTeam(String teamId, String title) {
        return new SearchPublicationSpecification(null, Set.of(teamId), title, null, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchPublicationSpecification that = (SearchPublicationSpecification) o;
        return Objects.equals(deletion, that.deletion) && Objects.equals(teamIds, that.teamIds) && Objects.equals(title, that.title) && publishedStatus == that.publishedStatus && Objects.equals(minPublishedDate, that.minPublishedDate) && Objects.equals(maxPublishedDate, that.maxPublishedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletion, teamIds, title, publishedStatus, minPublishedDate, maxPublishedDate);
    }
}
