package info.tomacla.biketeam.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChangeEmailFormTest {

    @Test
    public void testDefaultEmptyForm() {

        final ChangeEmailForm form = ChangeEmailForm.builder().get();

        assertEquals("", form.getEmail());
        assertEquals("", form.getCurrentPassword());
        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testEmailIsNormalized() {

        final ChangeEmailForm form = ChangeEmailForm.builder().withEmail(" New.User@EXAMPLE.com  ").get();

        assertEquals("new.user@example.com", form.parser().getEmail());

    }

    @Test
    public void testInvalidEmailIsRejected() {

        final ChangeEmailForm form = ChangeEmailForm.builder().withEmail("user@").get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testCurrentPasswordKeepsCaseAndInnerSpaces() {

        final ChangeEmailForm form = ChangeEmailForm.builder().get();
        form.setCurrentPassword("  MyPassword 42 ");

        // seuls les espaces de bord sont retires (requireNonBlankOrDefault) : la casse et les
        // espaces internes du secret sont preserves
        assertEquals("MyPassword 42", form.parser().getCurrentPassword());

    }

    /**
     * Le builder n'expose aucune methode pour le mot de passe actuel : un formulaire reconstruit
     * pour reaffichage ne peut jamais reinjecter le secret saisi dans la vue.
     */
    @Test
    public void testBuilderNeverCarriesCurrentPassword() {

        final ChangeEmailForm form = ChangeEmailForm.builder().withEmail("user@example.com").get();

        assertEquals("", form.getCurrentPassword());

    }

    @Test
    public void testBlankCurrentPasswordFallsBackToEmptyString() {

        final ChangeEmailForm form = ChangeEmailForm.builder().get();
        form.setCurrentPassword("   ");

        assertEquals("", form.getCurrentPassword());

    }

}
