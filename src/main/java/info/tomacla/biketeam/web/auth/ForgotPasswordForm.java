package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.common.datatype.Strings;

/**
 * Formulaire "mot de passe oublie".
 */
public class ForgotPasswordForm {

    private String email = "";

    public static ForgotPasswordFormBuilder builder() {
        return new ForgotPasswordFormBuilder();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrDefault(email, "");
    }

    public ForgotPasswordFormParser parser() {
        return new ForgotPasswordFormParser(this);
    }

    public static class ForgotPasswordFormParser {

        private final ForgotPasswordForm form;

        public ForgotPasswordFormParser(ForgotPasswordForm form) {
            this.form = form;
        }

        public String getEmail() {
            if (!Strings.isEmail(form.getEmail())) {
                throw new IllegalArgumentException("L'adresse email est invalide.");
            }
            return form.getEmail().trim().toLowerCase();
        }

    }

    public static class ForgotPasswordFormBuilder {

        private final ForgotPasswordForm form;

        public ForgotPasswordFormBuilder() {
            this.form = new ForgotPasswordForm();
        }

        public ForgotPasswordFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public ForgotPasswordForm get() {
            return form;
        }

    }

}
