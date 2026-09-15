package info.tomacla.biketeam.web.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordFormTest {

    @Test
    public void testDefaultEmptyForm() {

        final ForgotPasswordForm form = ForgotPasswordForm.builder().get();

        assertEquals("", form.getEmail());
        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

    @Test
    public void testBlankEmailFallsBackToEmptyString() {

        final ForgotPasswordForm form = ForgotPasswordForm.builder().withEmail("   ").get();

        assertEquals("", form.getEmail());

    }

    @Test
    public void testNullEmailFallsBackToEmptyString() {

        final ForgotPasswordForm form = ForgotPasswordForm.builder().withEmail(null).get();

        assertEquals("", form.getEmail());

    }

    @Test
    public void testEmailIsNormalized() {

        final ForgotPasswordForm form = ForgotPasswordForm.builder().withEmail("  USER@Example.COM ").get();

        assertEquals("user@example.com", form.parser().getEmail());

    }

    @Test
    public void testInvalidEmailIsRejected() {

        final ForgotPasswordForm form = ForgotPasswordForm.builder().withEmail("not-an-email").get();

        assertThrows(IllegalArgumentException.class, () -> form.parser().getEmail());

    }

}
