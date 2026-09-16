package info.tomacla.biketeam.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPasskeyRepository extends JpaRepository<UserPasskey, String> {

    Optional<UserPasskey> findByCredentialId(String credentialId);

    List<UserPasskey> findByUserIdOrderByCreatedAtAsc(String userId);

    Optional<UserPasskey> findByIdAndUserId(String id, String userId);

    long countByUserId(String userId);

    @Modifying
    @Query("delete from UserPasskey p where p.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    /**
     * Rattache les passkeys d'un compte absorbe au compte conserve.
     * <p>
     * Aucun dedoublonnage n'est necessaire : credential_id est unique a l'echelle de la table,
     * un meme authentificateur ne peut donc pas figurer sur les deux comptes.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "update user_passkey set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveToUser(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

}
