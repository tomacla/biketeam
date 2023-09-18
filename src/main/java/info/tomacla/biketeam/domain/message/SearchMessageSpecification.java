package info.tomacla.biketeam.domain.message;


import info.tomacla.biketeam.domain.user.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

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

}
