package info.tomacla.biketeam.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterFormTest {

    @Test
    public void test() {

        final RegisterForm form = RegisterForm.builder()
                .withFirstName("Jean")
                .withLastName("Dupont")
                .withEmail("Jean.Dupont@Example.COM")
                .get();

        final RegisterForm.RegisterFormParser parser = form.parser();

        assertEquals("Jean", parser.getFirstName());
        assertEquals("Dupont", parser.getLastName());
        assertEquals("jean.dupont@example.com", parser.getEmail());

    }

    @Test
    public void testBlankFirstNameRejected() {

        final RegisterForm form = RegisterForm.builder()
                .withFirstName("")
                .withLastName("Dupont")
                .withEmail("jean.dupont@example.com")
                .get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getFirstName());

    }

    @Test
    public void testBlankLastNameRejected() {

        final RegisterForm form = RegisterForm.builder()
                .withFirstName("Jean")
                .withLastName("")
                .withEmail("jean.dupont@example.com")
                .get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getLastName());

    }

    @Test
    public void testInvalidEmailRejected() {

        final RegisterForm form = RegisterForm.builder()
                .withFirstName("Jean")
                .withLastName("Dupont")
                .withEmail("not-an-email")
                .get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testBuilderNeverReinjectsPassword() {

        // le builder n'expose aucune methode de mot de passe : impossible de reafficher le
        // secret saisi dans un formulaire reconstruit apres une erreur.
        final RegisterForm form = RegisterForm.builder()
                .withFirstName("Jean")
                .withLastName("Dupont")
                .withEmail("jean.dupont@example.com")
                .get();

        assertEquals("", form.getPassword());
        assertEquals("", form.getPasswordConfirm());

    }

}
