package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class SearchTripSpecification implements Specification<Trip> {

    private final Boolean deletion;
    private final String permalink;
    private final Boolean listedInFeed;
    private final User user;
    private final Set<String> teamIds;
    private final String title;
    private final PublishedStatus publishedStatus;
    private final ZonedDateTime minPublishedDate;
    private final ZonedDateTime maxPublishedDate;
    private final LocalDate minStartDate;
    private final LocalDate maxStartDate;

    public SearchTripSpecification(Boolean deletion, String permalink, String title, Boolean listedInFeed, User user, Set<String> teamIds, PublishedStatus publishedStatus, ZonedDateTime minPublishedDate, ZonedDateTime maxPublishedDate, LocalDate minStartDate, LocalDate maxStartDate) {
        this.deletion = deletion;
        this.permalink = permalink;
        this.title = title;
        this.listedInFeed = listedInFeed;
        this.user = user;
        this.teamIds = teamIds;
        this.publishedStatus = publishedStatus;
        this.minPublishedDate = minPublishedDate;
        this.maxPublishedDate = maxPublishedDate;
        this.minStartDate = minStartDate;
        this.maxStartDate = maxStartDate;
    }

    @Override
    public Predicate toPredicate(Root<Trip> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();
        if (deletion != null) {
            predicates.add(criteriaBuilder.equal(root.get("deletion"), deletion.booleanValue()));
        }
        if (listedInFeed != null) {
            predicates.add(criteriaBuilder.equal(root.get("listedInFeed"), listedInFeed.booleanValue()));
        }
        if (title != null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (user != null) {
            predicates.add(criteriaBuilder.isMember(user, root.get("participants")));
        }
        if (teamIds != null && !teamIds.isEmpty()) {
            final CriteriaBuilder.In<Object> teamId = criteriaBuilder.in(root.get("teamId"));
            teamIds.forEach(teamId::value);
            predicates.add(teamId);
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
        if (minStartDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), minStartDate));
        }
        if (maxStartDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), maxStartDate));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchTripSpecification readyForDeletion() {
        return new SearchTripSpecification(true, null, null, null, null, null, null, null, null, null, null);
    }


}
