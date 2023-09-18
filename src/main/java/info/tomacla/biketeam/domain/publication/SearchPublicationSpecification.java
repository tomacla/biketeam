package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

}
