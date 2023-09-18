package info.tomacla.biketeam.domain.map;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Set;

@Repository
public interface MapRepository extends PagingAndSortingRepository<Map, String>, JpaSpecificationExecutor<Map> {

    @Query(value = "select distinct mt.tags from map_tags mt, map m where m.team_id = :teamId and m.id = mt.map_id order by mt.tags", nativeQuery = true)
    Set<String> findAllDistinctTags(@Param("teamId") String teamId);

    @Query(value = "select distinct mt.tags from map_tags mt, map m where m.team_id = :teamId and m.id = mt.map_id and LOWER(mt.tags) LIKE %:tagPart% order by mt.tags", nativeQuery = true)
    Set<String> findDistinctTagsContaining(@Param("teamId") String teamId, @Param("tagPart") String tagPart);

    @Transactional
    @Modifying
    @Query(value = "update ride_group set map_id = NULL where map_id = :removedMapId", nativeQuery = true)
    void removeMapIdInGroups(@Param("removedMapId") String removedMapId);

    @Transactional
    @Modifying
    @Query(value = "update trip_stage set map_id = NULL where map_id = :removedMapId", nativeQuery = true)
    void removeMapIdInStages(@Param("removedMapId") String removedMapId);

}
