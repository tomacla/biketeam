package info.tomacla.biketeam.domain.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthTokenRepository extends CrudRepository<UserAuthToken, String> {

    Optional<UserAuthToken> findByTokenHash(String tokenHash);

    List<UserAuthToken> findByUserIdAndTypeAndConsumedAtIsNull(String userId, UserAuthTokenType type);

    long countByUserIdAndTypeAndCreatedAtAfter(String userId, UserAuthTokenType type, Instant after);

    long countByRelatedUserIdAndTypeAndCreatedAtAfter(String relatedUserId, UserAuthTokenType type, Instant after);

    void deleteByExpiresAtBefore(Instant before);

    /**
     * Fusion de comptes : les tokens en vol du compte absorbe sont invalides, jamais deplaces.
     * Les deplacer serait une faille : un lien de reinitialisation emis sur le compte Strava
     * deviendrait, apres fusion, un droit de poser un mot de passe sur le compte conserve.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update user_auth_token set consumed_at = :now "
            + "where user_id = :userId and consumed_at is null", nativeQuery = true)
    int consumeAllForUser(@Param("userId") String userId, @Param("now") Instant now);

}
