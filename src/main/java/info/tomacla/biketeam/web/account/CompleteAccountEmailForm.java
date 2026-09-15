package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.common.datatype.Strings;

/**
 * Saisie de l'adresse email lors de la completion forcee d'un compte.
 * <p>
 * Comme pour /users/me/email, l'adresse n'est jamais posee directement sur le compte : elle est
 * portee par le token de verification (target_email) et n'est reprise qu'apres confirmation.
 */
public class CompleteAccountEmailForm {

    private String email = "";

    public static CompleteAccountEmailFormBuilder builder() {
        return new CompleteAccountEmailFormBuilder();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrDefault(email, "");
    }

    public CompleteAccountEmailFormParser parser() {
        return new CompleteAccountEmailFormParser(this);
    }

    public static class CompleteAccountEmailFormParser {

        private final CompleteAccountEmailForm form;

        public CompleteAccountEmailFormParser(CompleteAccountEmailForm form) {
            this.form = form;
        }

        public String getEmail() {
            if (!Strings.isEmail(form.getEmail())) {
                throw new IllegalArgumentException("L'adresse email est invalide.");
            }
            return form.getEmail().trim().toLowerCase();
        }

    }

    public static class CompleteAccountEmailFormBuilder {

        private final CompleteAccountEmailForm form;

        public CompleteAccountEmailFormBuilder() {
            this.form = new CompleteAccountEmailForm();
        }

        public CompleteAccountEmailFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public CompleteAccountEmailForm get() {
            return form;
        }

    }

}
