package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.common.datatype.Strings;

/**
 * Formulaire de definition d'un nouveau mot de passe a partir d'un lien recu par email.
 * <p>
 * Le builder n'expose que le code du lien : le mot de passe saisi n'est jamais reinjecte
 * dans le modele lors du reaffichage du formulaire en erreur.
 */
public class ResetPasswordForm {

    private String code = "";
    private String password = "";
    private String passwordConfirm = "";

    public static ResetPasswordFormBuilder builder() {
        return new ResetPasswordFormBuilder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = Strings.requireNonBlankOrDefault(code, "");
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

    public ResetPasswordFormParser parser() {
        return new ResetPasswordFormParser(this);
    }

    public static class ResetPasswordFormParser {

        private final ResetPasswordForm form;

        public ResetPasswordFormParser(ResetPasswordForm form) {
            this.form = form;
        }

        public String getCode() {
            return Strings.requireNonBlank(form.getCode(), "Ce lien n'est plus valide. Demandez-en un nouveau.");
        }

        public String getPassword() {
            return form.getPassword();
        }

        public String getPasswordConfirm() {
            return form.getPasswordConfirm();
        }

    }

    public static class ResetPasswordFormBuilder {

        private final ResetPasswordForm form;

        public ResetPasswordFormBuilder() {
            this.form = new ResetPasswordForm();
        }

        public ResetPasswordFormBuilder withCode(String code) {
            form.setCode(code);
            return this;
        }

        public ResetPasswordForm get() {
            return form;
        }

    }

}
