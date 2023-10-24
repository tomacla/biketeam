package info.tomacla.biketeam.domain.message;


import info.tomacla.biketeam.domain.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchMessageSpecification implements Specification<Message> {

    private final String targetId;
    private final User user;
    private final MessageTargetType type;

    public SearchMessageSpecification(String targetId, User user, MessageTargetType type) {
        this.targetId = targetId;
        this.user = user;
        this.type = type;
    }

    @Override
    public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (targetId != null && !targetId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("targetId"), targetId));
        }
        if (user != null) {
            predicates.add(criteriaBuilder.equal(root.get("user"), user));
        }
        if (type != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), type));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchMessageSpecification byTargetAndType(String targetId, MessageTargetType type) {
        return new SearchMessageSpecification(targetId, null, type);
    }

    public static SearchMessageSpecification byUser(User user) {
        return new SearchMessageSpecification(null, user, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchMessageSpecification that = (SearchMessageSpecification) o;
        return Objects.equals(targetId, that.targetId) && Objects.equals(user, that.user) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, user, type);
    }
}
