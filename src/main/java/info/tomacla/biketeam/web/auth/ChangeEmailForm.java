package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.common.datatype.Strings;

/**
 * Formulaire de demande de changement d'adresse email depuis /users/me.
 * <p>
 * La saisie ne modifie jamais directement {@code user_account.email} : elle declenche l'envoi
 * d'un lien de verification portant l'adresse visee (token.target_email).
 * <p>
 * Le mot de passe actuel est exige des lors que le compte en possede un : l'adresse email est une
 * identite de connexion, la changer sans preuve de possession permettrait a un simple cookie
 * remember-me vole de s'approprier definitivement le compte.
 */
public class ChangeEmailForm {

    private String email = "";

    private String currentPassword = "";

    public static ChangeEmailFormBuilder builder() {
        return new ChangeEmailFormBuilder();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrDefault(email, "");
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = Strings.requireNonBlankOrDefault(currentPassword, "");
    }

    public ChangeEmailFormParser parser() {
        return new ChangeEmailFormParser(this);
    }

    public static class ChangeEmailFormParser {

        private final ChangeEmailForm form;

        public ChangeEmailFormParser(ChangeEmailForm form) {
            this.form = form;
        }

        public String getEmail() {
            if (!Strings.isEmail(form.getEmail())) {
                throw new IllegalArgumentException("L'adresse email est invalide.");
            }
            return form.getEmail().trim().toLowerCase();
        }

        /**
         * Mot de passe actuel, jamais reinjecte dans la vue.
         * <p>
         * Seuls les espaces de bord sont retires par le setter : un mot de passe commencant ou
         * finissant par un espace ne pourra donc pas etre saisi ici. La casse et le contenu interne
         * sont preserves tels quels.
         */
        public String getCurrentPassword() {
            return form.getCurrentPassword();
        }

    }

    public static class ChangeEmailFormBuilder {

        private final ChangeEmailForm form;

        public ChangeEmailFormBuilder() {
            this.form = new ChangeEmailForm();
        }

        public ChangeEmailFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public ChangeEmailForm get() {
            return form;
        }

    }

}
