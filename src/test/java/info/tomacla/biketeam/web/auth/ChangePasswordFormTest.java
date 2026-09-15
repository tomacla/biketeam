package info.tomacla.biketeam.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordFormTest {

    @Test
    public void testDefaultEmptyForm() {

        final ChangePasswordForm form = ChangePasswordForm.builder().get();

        final ChangePasswordForm.ChangePasswordFormParser parser = form.parser();

        assertNull(parser.getCurrentPassword());
        assertEquals("", parser.getPassword());
        assertEquals("", parser.getPasswordConfirm());

    }

    @Test
    public void testCurrentPasswordNullWhenNotSet() {

        // un formulaire reconstruit (aucune methode de builder n'expose de champ) est toujours
        // vide : aucun secret saisi ne peut donc revenir dans le modele lors d'un reaffichage.
        final ChangePasswordForm form = ChangePasswordForm.builder().get();
        form.setPassword("newpassword123");
        form.setPasswordConfirm("newpassword123");

        assertNull(form.parser().getCurrentPassword());
        assertEquals("newpassword123", form.parser().getPassword());
        assertEquals("newpassword123", form.parser().getPasswordConfirm());

    }

    @Test
    public void testCurrentPasswordPresentWhenSet() {

        final ChangePasswordForm form = ChangePasswordForm.builder().get();
        form.setCurrentPassword("oldpassword123");

        assertEquals("oldpassword123", form.parser().getCurrentPassword());

    }

    @Test
    public void testBuilderExposesNoField() {

        // aucun builder n'expose de methode "with..." : impossible de reinjecter un secret
        // saisi dans un formulaire reconstruit pour reaffichage.
        final ChangePasswordForm form = ChangePasswordForm.builder().get();

        assertEquals("", form.getCurrentPassword());
        assertEquals("", form.getPassword());
        assertEquals("", form.getPasswordConfirm());

    }

}
