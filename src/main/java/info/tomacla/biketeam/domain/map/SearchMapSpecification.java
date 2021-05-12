package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.web.map.SearchMapForm;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class SearchMapSpecification implements Specification<Map> {

    private final SearchMapForm.SearchMapFormParser parser;

    public SearchMapSpecification(SearchMapForm form) {
        this.parser = form.parser();
    }

    @Override
    public Predicate toPredicate(Root<Map> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("length"), parser.getLowerDistance()));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("length"), parser.getUpperDistance()));
        if (!parser.getTags().isEmpty()) {
            predicates.add(root.join("tags").in(parser.getTags()));
        }
        if (parser.getWindDirection() != null) {
            predicates.add(criteriaBuilder.equal(root.get("windDirection"), parser.getWindDirection()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
