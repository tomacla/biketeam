package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.common.datatype.Strings;

/**
 * Formulaire d'inscription.
 * <p>
 * Le builder n'expose volontairement AUCUNE methode de mot de passe : un formulaire reconstruit
 * pour etre reaffiche apres une erreur ne peut donc jamais reinjecter le secret saisi dans le
 * modele, ni dans le HTML rendu.
 */
public class RegisterForm {

    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String password = "";
    private String passwordConfirm = "";

    public static RegisterFormBuilder builder() {
        return new RegisterFormBuilder();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Strings.requireNonBlankOrDefault(firstName, "");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Strings.requireNonBlankOrDefault(lastName, "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Strings.requireNonBlankOrDefault(email, "");
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

    public RegisterFormParser parser() {
        return new RegisterFormParser(this);
    }

    public static class RegisterFormParser {

        private final RegisterForm form;

        public RegisterFormParser(RegisterForm form) {
            this.form = form;
        }

        public String getFirstName() {
            return Strings.requireNonBlank(form.getFirstName(), "Le prénom est obligatoire.");
        }

        public String getLastName() {
            return Strings.requireNonBlank(form.getLastName(), "Le nom est obligatoire.");
        }

        public String getEmail() {
            if (!Strings.isEmail(form.getEmail())) {
                throw new IllegalArgumentException("L'adresse email est invalide.");
            }
            return form.getEmail().trim().toLowerCase();
        }

        public String getPassword() {
            return form.getPassword();
        }

        public String getPasswordConfirm() {
            return form.getPasswordConfirm();
        }

    }

    public static class RegisterFormBuilder {

        private final RegisterForm form;

        public RegisterFormBuilder() {
            this.form = new RegisterForm();
        }

        public RegisterFormBuilder withFirstName(String firstName) {
            form.setFirstName(firstName);
            return this;
        }

        public RegisterFormBuilder withLastName(String lastName) {
            form.setLastName(lastName);
            return this;
        }

        public RegisterFormBuilder withEmail(String email) {
            form.setEmail(email);
            return this;
        }

        public RegisterForm get() {
            return form;
        }

    }

}
