package info.tomacla.biketeam.web.user;

/**
 * Preferences de notification de l'utilisateur.
 * <p>
 * Les champs libres {@code email} et {@code stravaId} ont ete retires : ils permettaient de
 * poser n'importe quelle adresse ou n'importe quel identifiant Strava sur son propre compte.
 * L'adresse email etant devenue une identite de connexion, c'etait un vecteur d'usurpation
 * direct. L'adresse se change desormais par /users/me/email, avec verification par lien.
 */
public class EditUserForm {

    private String emailPublishTrips = null;
    private String emailPublishRides = null;
    private String emailPublishPublications = null;

    public static EditUserFormBuilder builder() {
        return new EditUserFormBuilder();
    }

    public String getEmailPublishTrips() {
        return emailPublishTrips;
    }

    public void setEmailPublishTrips(String emailPublishTrips) {
        this.emailPublishTrips = emailPublishTrips;
    }

    public String getEmailPublishRides() {
        return emailPublishRides;
    }

    public void setEmailPublishRides(String emailPublishRides) {
        this.emailPublishRides = emailPublishRides;
    }

    public String getEmailPublishPublications() {
        return emailPublishPublications;
    }

    public void setEmailPublishPublications(String emailPublishPublications) {
        this.emailPublishPublications = emailPublishPublications;
    }

    public EditUserFormParser parser() {
        return new EditUserFormParser(this);
    }

    public static class EditUserFormParser {

        private final EditUserForm form;

        public EditUserFormParser(EditUserForm form) {
            this.form = form;
        }

        public boolean isEmailPublishRides() {
            return form.getEmailPublishRides() != null && form.getEmailPublishRides().equals("on");
        }

        public boolean isEmailPublishTrips() {
            return form.getEmailPublishTrips() != null && form.getEmailPublishTrips().equals("on");
        }

        public boolean isEmailPublishPublications() {
            return form.getEmailPublishPublications() != null && form.getEmailPublishPublications().equals("on");
        }

    }

    public static class EditUserFormBuilder {

        private final EditUserForm form;

        public EditUserFormBuilder() {
            this.form = new EditUserForm();
        }

        public EditUserFormBuilder withEmailPublishRides(boolean emailPublishRides) {
            form.setEmailPublishRides(emailPublishRides ? "on" : null);
            return this;
        }

        public EditUserFormBuilder withEmailPublishTrips(boolean emailPublishTrips) {
            form.setEmailPublishTrips(emailPublishTrips ? "on" : null);
            return this;
        }

        public EditUserFormBuilder withEmailPublishPublications(boolean emailPublishPublications) {
            form.setEmailPublishPublications(emailPublishPublications ? "on" : null);
            return this;
        }

        public EditUserForm get() {
            return form;
        }
    }

}
