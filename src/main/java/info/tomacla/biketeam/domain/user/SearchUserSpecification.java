package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.team.Team;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static SearchUserSpecification byStravaId(Long stravaId) {
        return new SearchUserSpecification(false, null, null, false, stravaId, null, null, null, null);
    }

    public static SearchUserSpecification byFacebookId(String facebookId) {
        return new SearchUserSpecification(false, null, null, false, null, facebookId, null, null, null);
    }

    public static SearchUserSpecification byGoogleId(String googleId) {
        return new SearchUserSpecification(false, null, null, false, null, null, googleId, null, null);
    }

    public static SearchUserSpecification byEmail(String email) {
        return new SearchUserSpecification(false, null, null, false, null, null, null, email, null);
    }

    public static SearchUserSpecification admins() {
        return new SearchUserSpecification(false, true, null, false, null, null, null, null, null);
    }

    public static SearchUserSpecification byName(String name) {
        return new SearchUserSpecification(false, null, name, false, null, null, null, null, null);
    }

    public static SearchUserSpecification byNameInTeam(String name, Team team) {
        return new SearchUserSpecification(false, null, name, false, null, null, null, null, team);
    }

    public static SearchUserSpecification withEmailInTeam(Team team) {
        return new SearchUserSpecification(false, null, null, true, null, null, null, null, team);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchUserSpecification that = (SearchUserSpecification) o;
        return emailNotNull == that.emailNotNull && Objects.equals(deletion, that.deletion) && Objects.equals(admin, that.admin) && Objects.equals(name, that.name) && Objects.equals(stravaId, that.stravaId) && Objects.equals(facebookId, that.facebookId) && Objects.equals(googleId, that.googleId) && Objects.equals(email, that.email) && Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deletion, admin, name, emailNotNull, stravaId, facebookId, googleId, email, team);
    }
}
