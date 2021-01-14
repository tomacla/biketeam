package info.tomacla.biketeam.domain.map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapRepository extends CrudRepository<Map, String> {

    List<Map> findAll();

    List<Map> findByLengthBetweenAndVisibleTrue(double lowerDistance, double upperDistance);

    List<Map> findDistinctByLengthBetweenAndTagsInAndVisibleTrue(double lowerDistance, double upperDistance, List<String> tags);

    List<Map> findByVisibleTrue();

    List<MapIdNamePostedAtVisibleProjection> findAllByOrderByPostedAtDesc();

    List<MapIdNamePostedAtVisibleProjection> findAllByNameContainingIgnoreCaseOrderByPostedAtDesc(String namePart);

    @Query(value = "select distinct tags from map_tags order by tags", nativeQuery = true)
    List<String> findAllDistinctTags();

    @Query(value = "select distinct tags from map_tags where LOWER(tags) LIKE %:tagPart% order by tags", nativeQuery = true)
    List<String> findDistinctTagsContainer(@Param("tagPart") String tagPart);


}
