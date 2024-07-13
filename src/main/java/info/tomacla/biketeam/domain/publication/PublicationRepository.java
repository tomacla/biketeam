package info.tomacla.biketeam.domain.publication;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface PublicationRepository extends PagingAndSortingRepository<Publication, String>, JpaSpecificationExecutor<Publication> {

    @Transactional
    @Modifying
    @Query(value = "update publication_registration set user_email_code = NULL, user_email_valid = TRUE where user_email_code = :userEmailCode", nativeQuery = true)
    int validateEmail(@Param("userEmailCode") String userEmailCode);

}
