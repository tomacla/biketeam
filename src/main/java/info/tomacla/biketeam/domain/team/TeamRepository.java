package info.tomacla.biketeam.domain.team;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface TeamRepository extends CrudRepository<Team, String>, PagingAndSortingRepository<Team, String>, JpaSpecificationExecutor<Team> {

    @Query(value = "select t.id, max(r.published_at) as lastRidePublishedAt, max(p.published_at) as lastPublicationPublishedAt, max(tr.published_at) as lastTripPublishedAt from team t left outer join ride r on r.team_id = t.id left outer join publication p on p.team_id = t.id left outer join trip tr on tr.team_id = t.id where t.id in :teamIds group by t.id", nativeQuery = true)
    List<LastTeamData> findLastData(@Param("teamIds") Set<String> teamIds);

    // migration vers Pedalons : equipes gelees (MIGRATING), pretes a basculer (READY, gelees elles aussi) ou basculees
    List<Team> findByPedalonsStateIsNotNull();

    // mises a jour ciblees : ne pas reecrire toute l'equipe (et ses roles) depuis une copie en memoire.
    // Appelees dans une transaction ouverte par PedalonsMigrationService.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :state where t.id = :teamId")
    int updatePedalonsState(@Param("teamId") String teamId, @Param("state") PedalonsState state);

    /**
     * Gel pour une migration definitive : seulement depuis un groupe ordinaire ou pret a basculer
     * (la table d'URL en attente est alors perimee). Rend 0 si le groupe a ete bascule entre-temps.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :migrating, t.pedalonsUrl = null where t.id = :teamId and (t.pedalonsState is null or t.pedalonsState = :ready)")
    int freezePedalons(@Param("teamId") String teamId, @Param("migrating") PedalonsState migrating, @Param("ready") PedalonsState ready);

    default int freezePedalons(String teamId) {
        return freezePedalons(teamId, PedalonsState.MIGRATING, PedalonsState.READY);
    }

    /**
     * Abandon d'une bascule en attente (un nouvel essai la rend perimee). Rend 0 si le groupe
     * n'etait pas pret a basculer.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = null, t.pedalonsUrl = null where t.id = :teamId and t.pedalonsState = :ready")
    int clearPedalonsReady(@Param("teamId") String teamId, @Param("ready") PedalonsState ready);

    default int clearPedalonsReady(String teamId) {
        return clearPedalonsReady(teamId, PedalonsState.READY);
    }

    /**
     * Bascule demandee par l'administrateur du groupe : seulement depuis READY (conditionnel, donc
     * sans course avec le lancement d'une nouvelle migration). Rend 0 sinon.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :migrated, t.pedalonsMigratedAt = :migratedAt where t.id = :teamId and t.pedalonsState = :ready")
    int switchPedalons(@Param("teamId") String teamId, @Param("migrated") PedalonsState migrated,
                       @Param("ready") PedalonsState ready, @Param("migratedAt") Instant migratedAt);

    default int switchPedalons(String teamId, Instant migratedAt) {
        return switchPedalons(teamId, PedalonsState.MIGRATED, PedalonsState.READY, migratedAt);
    }

    /**
     * Fin reussie d'une migration definitive : seulement depuis MIGRATING (gel en cours). Rend 0 si
     * le groupe a ete degele ou change d'etat entre-temps.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :ready, t.pedalonsUrl = :url where t.id = :teamId and t.pedalonsState = :migrating")
    int readyPedalons(@Param("teamId") String teamId, @Param("url") String url,
                      @Param("ready") PedalonsState ready, @Param("migrating") PedalonsState migrating);

    default int readyPedalons(String teamId, String url) {
        return readyPedalons(teamId, url, PedalonsState.READY, PedalonsState.MIGRATING);
    }

    /**
     * Degel apres l'echec d'une migration definitive : seulement depuis MIGRATING. Rend 0 sinon.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = null where t.id = :teamId and t.pedalonsState = :migrating")
    int unfreezePedalons(@Param("teamId") String teamId, @Param("migrating") PedalonsState migrating);

    default int unfreezePedalons(String teamId) {
        return unfreezePedalons(teamId, PedalonsState.MIGRATING);
    }

    /**
     * Ecriture inconditionnelle des trois colonnes : reservee aux jeux d'essai et aux reprises
     * manuelles, le service passe par les transitions conditionnelles ci-dessus.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :state, t.pedalonsUrl = :url, t.pedalonsMigratedAt = :migratedAt where t.id = :teamId")
    int updatePedalonsSwitch(@Param("teamId") String teamId, @Param("state") PedalonsState state,
                             @Param("url") String url, @Param("migratedAt") Instant migratedAt);

}
