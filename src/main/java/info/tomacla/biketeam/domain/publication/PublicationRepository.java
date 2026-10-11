package info.tomacla.biketeam.domain.publication;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import jakarta.transaction.Transactional;

@Repository
public interface PublicationRepository extends CrudRepository<Publication, String>, PagingAndSortingRepository<Publication, String>, JpaSpecificationExecutor<Publication> {

    @Transactional
    @Modifying
    @Query(value = "update publication_registration set user_email_code = NULL, user_email_valid = TRUE where user_email_code = :userEmailCode", nativeQuery = true)
    int validateEmail(@Param("userEmailCode") String userEmailCode);

    // export vers Pedalons (TeamSnapshotBuilder) : graphe de chargement (FETCH) - ce qui est liste
    // vient dans la meme requete, tout le reste (y compris les associations EAGER : participants,
    // inscriptions...) n'est pas charge. Nombre de requetes independant du nombre d'elements.
    // aucune association exportee : le graphe ne nomme qu'un attribut simple, pour ne pas charger les inscriptions
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"title"})
    List<Publication> findByTeamId(String teamId);

    long countByTeamIdAndDeletionFalse(String teamId);

}
