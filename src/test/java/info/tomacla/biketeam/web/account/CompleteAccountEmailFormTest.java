package info.tomacla.biketeam.web.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompleteAccountEmailFormTest {

    @Test
    public void testDefaultEmptyForm() {

        final CompleteAccountEmailForm form = CompleteAccountEmailForm.builder().get();

        assertEquals("", form.getEmail());
        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testEmailIsNormalized() {

        final CompleteAccountEmailForm form = CompleteAccountEmailForm.builder()
                .withEmail(" Strava.User@EXAMPLE.com ").get();

        assertEquals("strava.user@example.com", form.parser().getEmail());

    }

    /**
     * Le formulaire est prerempli avec l'adresse du compte, qui peut etre nulle sur un compte
     * Strava : la construction ne doit pas echouer pour autant.
     */
    @Test
    public void testNullEmailFallsBackToEmptyString() {

        final CompleteAccountEmailForm form = CompleteAccountEmailForm.builder().withEmail(null).get();

        assertEquals("", form.getEmail());
        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testInvalidEmailIsRejected() {

        final CompleteAccountEmailForm form = CompleteAccountEmailForm.builder().withEmail("@example.com").get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

}
