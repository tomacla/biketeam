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

    List<MapIdNamePostedAtVisibleProjection> findAllByTeamIdOrderByPostedAtDesc(String teamId);

    List<MapIdNamePostedAtVisibleProjection> findAllByTeamIdAndNameContainingIgnoreCaseOrderByPostedAtDesc(String teamId, String namePart);

    Page<Map> findByTeamId(String teamId, Pageable pageable);

    @Query(value = "select distinct mt.tags from map_tags mt, map m where m.team_id = :teamId and m.id = mt.map_id order by mt.tags", nativeQuery = true)
    List<String> findAllDistinctTags(@Param("teamId") String teamId);

    @Query(value = "select distinct mt.tags from map_tags mt, map m where m.team_id = :teamId and m.id = mt.map_id and LOWER(mt.tags) LIKE %:tagPart% order by mt.tags", nativeQuery = true)
    List<String> findDistinctTagsContaining(@Param("teamId") String teamId, @Param("tagPart") String tagPart);

}
