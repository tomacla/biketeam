package info.tomacla.biketeam.domain.notification;


import info.tomacla.biketeam.domain.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchNotificationSpecification implements Specification<Notification> {

    private final Boolean viewed;
    private final User user;

    public SearchNotificationSpecification(Boolean viewed, User user) {
        this.viewed = viewed;
        this.user = user;
    }

    @Override
    public Predicate toPredicate(Root<Notification> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (user != null) {
            predicates.add(criteriaBuilder.equal(root.get("user"), user));
        }
        if (viewed != null) {
            predicates.add(criteriaBuilder.equal(root.get("viewed"), viewed.booleanValue()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchNotificationSpecification unviewedByUser(User user) {
        return new SearchNotificationSpecification(
                false, user
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchNotificationSpecification that = (SearchNotificationSpecification) o;
        return Objects.equals(viewed, that.viewed) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewed, user);
    }
}
