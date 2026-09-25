package info.tomacla.biketeam.service.pedalons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Suivi des migrations en cours : statut des jobs Pedalons, rejeu des declenchements
 * interrompus, expiration des demandes restees sans reponse. Inerte si la fonction est eteinte.
 */
@Service
public class PedalonsMigrationPoller {

    /** Pas du suivi ; borne le retard d'un rejeu de TRIGGERING (voir TRIGGERING_STUCK_AFTER). */
    public static final long INTERVAL_MS = 15000;

    private final PedalonsMigrationService migrationService;

    @Autowired
    public PedalonsMigrationPoller(PedalonsMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Scheduled(fixedDelay = INTERVAL_MS, initialDelay = 20000)
    public void poll() {
        migrationService.pollAll();
    }

}
