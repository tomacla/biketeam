package info.tomacla.biketeam.service.pedalons;

import info.tomacla.biketeam.service.export.TeamExportFiles;
import info.tomacla.biketeam.service.export.TeamFileKind;

/**
 * Chemins des fichiers dans l'instantane servi a Pedalons : URL relative a l'hote de l'endpoint
 * /internal/pedalons/teams/{teamId}/files/{kind}/{entityId}.
 */
public final class PedalonsFilePaths {

    public static final TeamExportFiles.FilePaths INSTANCE =
            (kind, teamId, entityId, file) -> filePath(teamId, kind, entityId);

    private PedalonsFilePaths() {
    }

    public static String filePath(String teamId, TeamFileKind kind, String entityId) {
        return "/internal/pedalons/teams/" + teamId + "/files/" + kind.getPathSegment() + "/" + entityId;
    }

}
