package info.tomacla.biketeam.service.auth;

import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.mail.MailSenderService;
import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Emails transactionnels d'authentification.
 * <p>
 * Point de vigilance couvert ici : sans SMTP configure, aucun envoi ne doit etre tente (le lien
 * est seulement journalise), et le lien envoye doit toujours porter le token en clair.
 */
public class AuthMailServiceTest {

    private MailSenderService mailSenderService;
    private UrlService urlService;
    private AuthMailService service;

    @BeforeEach
    public void setUp() {

        mailSenderService = mock(MailSenderService.class);
        urlService = mock(UrlService.class);

        service = new AuthMailService();
        ReflectionTestUtils.setField(service, "mailSenderService", mailSenderService);
        ReflectionTestUtils.setField(service, "urlService", urlService);

        when(mailSenderService.isSmtpConfigured()).thenReturn(true);
        when(urlService.getUrlWithSuffix(anyString()))
                .thenAnswer(invocation -> "https://biketeam.example" + invocation.getArgument(0));

    }

    private String captureBody(String expectedTo, String expectedSubject) {

        ArgumentCaptor<Set<String>> tos = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        verify(mailSenderService).sendDirectly(isNull(), tos.capture(), subject.capture(), body.capture(),
                nullable(ImageDescriptor.class));

        assertEquals(Set.of(expectedTo), tos.getValue());
        assertEquals(expectedSubject, subject.getValue());

        return body.getValue();

    }

    @Test
    public void testEmailVerificationCarriesTheClearTokenInTheLink() {

        service.sendEmailVerification("user@example.com", "clear-token-1");

        String body = captureBody("user@example.com", "Confirmez votre adresse email");

        assertTrue(body.contains("https://biketeam.example/verify-email?code=clear-token-1"), body);
        verify(urlService).getUrlWithSuffix("/verify-email?code=clear-token-1");

    }

    @Test
    public void testPasswordResetCarriesTheClearTokenInTheLink() {

        service.sendPasswordReset("user@example.com", "clear-token-2");

        String body = captureBody("user@example.com", "Réinitialisation de votre mot de passe");

        assertTrue(body.contains("https://biketeam.example/reset-password?code=clear-token-2"), body);

    }

    /**
     * Le lien "definir un mot de passe" est envoye a un compte preexistant : il doit pointer vers
     * la reinitialisation, jamais vers une creation de compte.
     */
    @Test
    public void testDefinePasswordUsesTheResetPasswordLink() {

        service.sendDefinePassword("user@example.com", "clear-token-3");

        String body = captureBody("user@example.com", "Définissez votre mot de passe");

        assertTrue(body.contains("/reset-password?code=clear-token-3"), body);

    }

    /**
     * Le message "compte existant" ne doit contenir aucun lien : il est envoye a une adresse dont
     * quelqu'un d'autre a peut-etre tente de creer le compte.
     */
    @Test
    public void testAccountAlreadyExistsCarriesNoToken() {

        service.sendAccountAlreadyExists("user@example.com");

        String body = captureBody("user@example.com", "Tentative de création de compte");

        assertFalse(body.contains("href"), body);
        verify(urlService, never()).getUrlWithSuffix(anyString());

    }

    /**
     * L'alerte de changement d'adresse part vers l'ANCIENNE adresse, et mentionne la nouvelle.
     */
    @Test
    public void testEmailChangeAlertIsSentToTheOldAddress() {

        service.sendEmailChangeAlert("old@example.com", "new@example.com");

        String body = captureBody("old@example.com", "Votre adresse email a été modifiée");

        assertTrue(body.contains("new@example.com"), body);

    }

    @Test
    public void testNothingIsSentWhenSmtpIsNotConfigured() {

        when(mailSenderService.isSmtpConfigured()).thenReturn(false);

        service.sendEmailVerification("user@example.com", "clear-token");
        service.sendPasswordReset("user@example.com", "clear-token");
        service.sendDefinePassword("user@example.com", "clear-token");
        service.sendAccountAlreadyExists("user@example.com");
        service.sendEmailChangeAlert("old@example.com", "new@example.com");

        verify(mailSenderService, never()).sendDirectly(any(Team.class), anySet(), anyString(), anyString(), any());
        verify(mailSenderService, never()).sendDirectly(isNull(), anySet(), anyString(), anyString(), any());

    }

}
