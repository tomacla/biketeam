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
import java.util.Objects;
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
        if (permalink != null && !permalink.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("permalink"), permalink));
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

    public static SearchTripSpecification allInTeam(String teamId) {
        return new SearchTripSpecification(null, null, null, null, null, Set.of(teamId), null, null, null, null, null);
    }

    public static SearchTripSpecification byTitleInTeam(String teamId, String title) {
        return new SearchTripSpecification(false, null, title, null, null, Set.of(teamId), null, null, null, null, null);
    }

    public static SearchTripSpecification upcomingByUser(User user, Set<String> teamIds, LocalDate from, LocalDate to) {
        return new SearchTripSpecification(
                false, null, null, null, user, teamIds, PublishedStatus.PUBLISHED, null, null,
                from, to);
    }

    public static SearchTripSpecification byPermalink(String permalink) {
        return new SearchTripSpecification(
                false, permalink, null, null, null, null, null, null, null, null, null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchTripSpecification that = (SearchTripSpecification) o;
        return Objects.equals(deletion, that.deletion) && Objects.equals(permalink, that.permalink) && Objects.equals(listedInFeed, that.listedInFeed) && Objects.equals(user, that.user) && Objects.equals(teamIds, that.teamIds) && Objects.equals(title, that.title) && publishedStatus == that.publishedStatus && Objects.equals(minPublishedDate, that.minPublishedDate) && Objects.equals(maxPublishedDate, that.maxPublishedDate) && Objects.equals(minStartDate, that.minStartDate) && Objects.equals(maxStartDate, that.maxStartDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletion, permalink, listedInFeed, user, teamIds, title, publishedStatus, minPublishedDate, maxPublishedDate, minStartDate, maxStartDate);
    }
}
