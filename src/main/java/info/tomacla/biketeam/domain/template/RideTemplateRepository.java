package info.tomacla.biketeam.domain.template;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideTemplateRepository extends CrudRepository<RideTemplate, String>, PagingAndSortingRepository<RideTemplate, String>, JpaSpecificationExecutor<RideTemplate> {

    // export vers Pedalons (TeamSnapshotBuilder) : graphe de chargement (FETCH) - ce qui est liste
    // vient dans la meme requete, tout le reste (y compris les associations EAGER : participants,
    // inscriptions...) n'est pas charge. Nombre de requetes independant du nombre d'elements.
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"startPlace", "endPlace", "groups"})
    List<RideTemplate> findByTeamId(String teamId);

    long countByTeamId(String teamId);

}
