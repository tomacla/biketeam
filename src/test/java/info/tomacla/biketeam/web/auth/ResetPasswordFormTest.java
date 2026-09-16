package info.tomacla.biketeam.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResetPasswordFormTest {

    @Test
    public void test() {

        final ResetPasswordForm form = ResetPasswordForm.builder()
                .withCode("abc123")
                .get();

        final ResetPasswordForm.ResetPasswordFormParser parser = form.parser();

        assertEquals("abc123", parser.getCode());

    }

    @Test
    public void testBlankCodeRejected() {

        final ResetPasswordForm form = ResetPasswordForm.builder()
                .withCode("")
                .get();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> form.parser().getCode());
        assertEquals("Ce lien n'est plus valide. Demandez-en un nouveau.", ex.getMessage());

    }

    @Test
    public void testBuilderNeverReinjectsPassword() {

        // le builder n'expose que le code : le mot de passe saisi ne peut jamais revenir
        // dans le modele lors d'un reaffichage en erreur.
        final ResetPasswordForm form = ResetPasswordForm.builder()
                .withCode("abc123")
                .get();

        assertEquals("", form.getPassword());
        assertEquals("", form.getPasswordConfirm());

    }

}
