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

    // migration vers Pedalons : equipes basculees
    List<Team> findByPedalonsStateIsNotNull();

    // mises a jour ciblees : ne pas reecrire toute l'equipe (et ses roles) depuis une copie en memoire.
    // Appelees dans une transaction ouverte par PedalonsMigrationService.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :state where t.id = :teamId")
    int updatePedalonsState(@Param("teamId") String teamId, @Param("state") PedalonsState state);

    /**
     * Bascule demandee par l'administrateur du groupe : seulement depuis un groupe ordinaire
     * (conditionnel, donc sans double bascule). Rend 0 si le groupe est deja bascule.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Team t set t.pedalonsState = :migrated, t.pedalonsUrl = :url, t.pedalonsMigratedAt = :migratedAt where t.id = :teamId and t.pedalonsState is null")
    int switchPedalons(@Param("teamId") String teamId, @Param("url") String url, @Param("migrated") PedalonsState migrated,
                       @Param("migratedAt") Instant migratedAt);

    default int switchPedalons(String teamId, String url, Instant migratedAt) {
        return switchPedalons(teamId, url, PedalonsState.MIGRATED, migratedAt);
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
