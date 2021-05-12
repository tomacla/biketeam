package info.tomacla.biketeam.domain.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MapRepository extends PagingAndSortingRepository<Map, String>, JpaSpecificationExecutor<Map> {

    List<Map> findAll();

    Page<Map> findDistinctByLengthBetweenAndWindDirectionAndTagsInAndVisibleTrue(double lowerDistance, double upperDistance, WindDirection windDirection, List<String> tags, Pageable pageable);

    Page<Map> findByLengthBetweenAndWindDirectionAndVisibleTrue(double lowerDistance, double upperDistance, WindDirection windDirection, Pageable pageable);

    List<MapIdNamePostedAtVisibleProjection> findAllByOrderByPostedAtDesc();

    List<MapIdNamePostedAtVisibleProjection> findAllByNameContainingIgnoreCaseOrderByPostedAtDesc(String namePart);

    @Query(value = "select distinct tags from map_tags order by tags", nativeQuery = true)
    List<String> findAllDistinctTags();

    @Query(value = "select distinct tags from map_tags where LOWER(tags) LIKE %:tagPart% order by tags", nativeQuery = true)
    List<String> findDistinctTagsContainer(@Param("tagPart") String tagPart);


}
