package info.tomacla.biketeam.domain.map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MapRatingRepository extends CrudRepository<MapRating, String> {

    Optional<MapRating> findByMapIdAndUserId(String mapId, String userId);

    @Query("SELECT AVG(mr.rating) FROM MapRating mr WHERE mr.map.id = :mapId")
    Double findAverageRatingByMapId(@Param("mapId") String mapId);

    @Query("SELECT COUNT(mr) FROM MapRating mr WHERE mr.map.id = :mapId")
    Long countRatingsByMapId(@Param("mapId") String mapId);

}