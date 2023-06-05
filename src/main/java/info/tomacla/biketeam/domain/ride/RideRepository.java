package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {

    Optional<Ride> findByPermalink(String permalink);


    List<Ride> findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(boolean deletion, String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Ride> findAllByDeletionAndTeamIdInAndDateBetweenAndPublishedStatus(boolean deletion, Set<String> teamIds, LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<RideProjection> findAllByDeletionAndTeamIdOrderByDateDesc(boolean deletion, String teamId);

    List<RideProjection> findAllByDeletion(boolean deletion);

    @Transactional
    @Modifying
    @Query(value = "delete from ride_group_participant where user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("userId") String userId);

}
