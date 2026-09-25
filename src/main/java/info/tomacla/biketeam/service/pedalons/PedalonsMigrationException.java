package info.tomacla.biketeam.service.pedalons;

/**
 * Refus d'une operation de migration, avec un message destine a l'administrateur du groupe.
 */
public class PedalonsMigrationException extends RuntimeException {

    public PedalonsMigrationException(String message) {
        super(message);
    }

}
