package info.tomacla.biketeam.security.password;

import java.nio.charset.StandardCharsets;

/**
 * Politique de mot de passe : classe pure, sans dependance Spring, testable unitairement.
 * <p>
 * Pas de regle de composition (majuscules / chiffres / caracteres speciaux) : ces regles
 * sont contre-productives et poussent les utilisateurs vers des mots de passe previsibles.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 10;

    /**
     * BCrypt tronque silencieusement les entrees au dela de 72 octets : sans cette limite,
     * deux mots de passe distincts ouvriraient le meme compte.
     */
    public static final int MAX_BYTES = 72;

    public static final String MSG_TOO_SHORT = "Le mot de passe doit contenir au moins 10 caractères.";
    public static final String MSG_TOO_LONG = "Le mot de passe est trop long (72 octets maximum).";
    public static final String MSG_MISMATCH = "Les deux mots de passe ne correspondent pas.";
    public static final String MSG_CONTAINS_EMAIL = "Le mot de passe ne doit pas contenir votre adresse email.";

    private PasswordPolicy() {
    }

    /**
     * Valide un mot de passe et sa confirmation.
     *
     * @param password     mot de passe saisi
     * @param confirmation confirmation saisie
     * @param email        adresse email du compte (peut etre nulle)
     * @throws IllegalArgumentException si le mot de passe ne respecte pas la politique
     */
    public static void validate(String password, String confirmation, String email) {

        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(MSG_TOO_SHORT);
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IllegalArgumentException(MSG_TOO_LONG);
        }

        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException(MSG_MISMATCH);
        }

        if (email != null && !email.isBlank()) {

            final String normalizedEmail = email.trim().toLowerCase();
            final String lowerPassword = password.toLowerCase();

            if (password.equalsIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException(MSG_CONTAINS_EMAIL);
            }

            final int at = normalizedEmail.indexOf('@');
            final String localPart = (at > 0) ? normalizedEmail.substring(0, at) : normalizedEmail;

            if (localPart.length() >= 4 && lowerPassword.contains(localPart)) {
                throw new IllegalArgumentException(MSG_CONTAINS_EMAIL);
            }

        }

    }

}
