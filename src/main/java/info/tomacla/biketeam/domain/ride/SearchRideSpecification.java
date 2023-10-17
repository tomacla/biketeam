package info.tomacla.biketeam.domain.ride;

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


public class SearchRideSpecification implements Specification<Ride> {

    private final Boolean deletion;
    private final String permalink;
    private final Boolean listedInFeed;
    private final User user;
    private final String title;
    private final Set<String> teamIds;
    private final PublishedStatus publishedStatus;
    private final ZonedDateTime minPublishedDate;
    private final ZonedDateTime maxPublishedDate;

    private final LocalDate minDate;
    private final LocalDate maxDate;

    public SearchRideSpecification(Boolean deletion, String permalink, String title, Boolean listedInFeed, User user, Set<String> teamIds, PublishedStatus publishedStatus, ZonedDateTime minPublishedDate, ZonedDateTime maxPublishedDate, LocalDate minDate, LocalDate maxDate) {
        this.deletion = deletion;
        this.permalink = permalink;
        this.listedInFeed = listedInFeed;
        this.title = title;
        this.user = user;
        this.teamIds = teamIds;
        this.publishedStatus = publishedStatus;
        this.minPublishedDate = minPublishedDate;
        this.maxPublishedDate = maxPublishedDate;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    @Override
    public Predicate toPredicate(Root<Ride> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

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
            predicates.add(criteriaBuilder.isMember(user, root.join("groups").get("participants")));
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
        if (minDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), minDate));
        }
        if (maxDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), maxDate));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchRideSpecification readyForDeletion() {
        return new SearchRideSpecification(true, null, null, null, null, null, null, null, null, null, null);
    }

    public static SearchRideSpecification allInTeam(String teamId) {
        return new SearchRideSpecification(null,
                null, null, null, null, Set.of(teamId), null,
                null, null, null, null);
    }

    public static SearchRideSpecification byTitleInTeam(String teamId, String title) {
        return new SearchRideSpecification(false,
                null, title, null, null, Set.of(teamId), null,
                null, null, null, null);
    }

    public static SearchRideSpecification upcomingRidesByUser(Set<String> teamIds, User user, LocalDate from) {
        return new SearchRideSpecification(false,
                null,
                null,
                null,
                user,
                teamIds,
                PublishedStatus.PUBLISHED,
                null, null, from, null);
    }

    public static SearchRideSpecification byPermalink(String permalink) {
        return new SearchRideSpecification(false, permalink, null, null, null, null, null, null, null, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRideSpecification that = (SearchRideSpecification) o;
        return Objects.equals(deletion, that.deletion) && Objects.equals(permalink, that.permalink) && Objects.equals(listedInFeed, that.listedInFeed) && Objects.equals(user, that.user) && Objects.equals(title, that.title) && Objects.equals(teamIds, that.teamIds) && publishedStatus == that.publishedStatus && Objects.equals(minPublishedDate, that.minPublishedDate) && Objects.equals(maxPublishedDate, that.maxPublishedDate) && Objects.equals(minDate, that.minDate) && Objects.equals(maxDate, that.maxDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletion, permalink, listedInFeed, user, title, teamIds, publishedStatus, minPublishedDate, maxPublishedDate, minDate, maxDate);
    }
}
