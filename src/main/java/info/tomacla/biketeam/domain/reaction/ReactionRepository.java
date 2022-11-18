package info.tomacla.biketeam.domain.reaction;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends CrudRepository<Reaction, String> {

    List<Reaction> findAll();

    Optional<Reaction> findByTargetIdAndUserId(String targetId, String userId);

    List<Reaction> findAllByTargetIdAndType(String targetId, ReactionTargetType type);

    List<Reaction> findAllByUserId(String userId);

    @Transactional
    @Modifying
    @Query(value = "delete from reaction where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "delete from reaction where target_id = :targetId", nativeQuery = true)
    void deleteByTargetId(@Param("targetId") String targetId);

}