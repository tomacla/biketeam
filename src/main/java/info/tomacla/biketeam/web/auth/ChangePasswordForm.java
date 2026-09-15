package info.tomacla.biketeam.web.auth;

/**
 * Formulaire de changement (ou de premiere definition) de mot de passe depuis /users/me.
 * <p>
 * {@code currentPassword} n'est exige que si le compte porte deja un mot de passe. Le builder
 * n'expose aucun champ : un formulaire reconstruit est toujours vide, aucun secret saisi ne
 * peut donc revenir dans le modele.
 */
public class ChangePasswordForm {

    private String currentPassword = "";
    private String password = "";
    private String passwordConfirm = "";

    public static ChangePasswordFormBuilder builder() {
        return new ChangePasswordFormBuilder();
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword == null ? "" : currentPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm == null ? "" : passwordConfirm;
    }

    public ChangePasswordFormParser parser() {
        return new ChangePasswordFormParser(this);
    }

    public static class ChangePasswordFormParser {

        private final ChangePasswordForm form;

        public ChangePasswordFormParser(ChangePasswordForm form) {
            this.form = form;
        }

        /**
         * Mot de passe actuel, ou null s'il n'a pas ete saisi.
         */
        public String getCurrentPassword() {
            return form.getCurrentPassword().isEmpty() ? null : form.getCurrentPassword();
        }

        public String getPassword() {
            return form.getPassword();
        }

        public String getPasswordConfirm() {
            return form.getPasswordConfirm();
        }

    }

    public static class ChangePasswordFormBuilder {

        private final ChangePasswordForm form;

        public ChangePasswordFormBuilder() {
            this.form = new ChangePasswordForm();
        }

        public ChangePasswordForm get() {
            return form;
        }

    }

}
