package info.tomacla.biketeam.security.passkey;

import java.util.Base64;

/**
 * Encodage base64url sans remplissage, format impose par WebAuthn pour tous les champs binaires
 * echanges avec le navigateur.
 */
public final class Base64Url {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private Base64Url() {
    }

    public static String encode(byte[] value) {
        return ENCODER.encodeToString(value);
    }

    /**
     * @return null si la valeur est absente ou n'est pas du base64url valide. Les entrees
     * viennent du navigateur : une valeur malformee est un cas nominal, pas une anomalie.
     */
    public static byte[] decodeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            // certains clients renvoient du base64url AVEC remplissage : le decodeur
            // l'accepte, mais pas les caracteres du base64 standard
            return DECODER.decode(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
