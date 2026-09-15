package info.tomacla.biketeam.domain.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthTokenRepository extends CrudRepository<UserAuthToken, String> {

    Optional<UserAuthToken> findByTokenHash(String tokenHash);

    List<UserAuthToken> findByUserIdAndTypeAndConsumedAtIsNull(String userId, UserAuthTokenType type);

    long countByUserIdAndTypeAndCreatedAtAfter(String userId, UserAuthTokenType type, Instant after);

    void deleteByExpiresAtBefore(Instant before);

}
