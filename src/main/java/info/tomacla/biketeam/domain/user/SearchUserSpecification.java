package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.team.Team;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SearchUserSpecification implements Specification<User> {

    private final Boolean deletion;
    private final Boolean admin;
    private final String name;
    private final boolean emailNotNull;
    private final Long stravaId;
    private final String facebookId;
    private final String googleId;
    private final String email;
    private final Team team;

    public SearchUserSpecification(Boolean deletion, Boolean admin, String name, boolean emailNotNull, Long stravaId, String facebookId, String googleId, String email, Team team) {
        this.deletion = deletion;
        this.admin = admin;
        this.name = name;
        this.emailNotNull = emailNotNull;
        this.stravaId = stravaId;
        this.facebookId = facebookId;
        this.googleId = googleId;
        this.email = email;
        this.team = team;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (deletion != null) {
            predicates.add(criteriaBuilder.equal(root.get("deletion"), deletion.booleanValue()));
        }
        if (admin != null) {
            predicates.add(criteriaBuilder.equal(root.get("admin"), admin.booleanValue()));
        }
        if (name != null) {
            predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + name.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + name.toLowerCase() + "%")));
        }
        if (emailNotNull) {
            predicates.add(criteriaBuilder.isNotNull(root.get("email")));
        }
        if (stravaId != null) {
            predicates.add(criteriaBuilder.equal(root.get("stravaId"), stravaId));
        }
        if (facebookId != null && !facebookId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("facebookId"), facebookId));
        }
        if (googleId != null && !googleId.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("googleId"), googleId));
        }
        if (email != null && !email.isBlank()) {
            predicates.add(criteriaBuilder.equal(root.get("email"), email));
        }
        if (team != null) {
            predicates.add(criteriaBuilder.equal(root.join("roles").get("team"), team));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    public static SearchUserSpecification readyForDeletion() {
        return new SearchUserSpecification(true, null, null, false, null, null, null, null, null);
    }

}
