package info.tomacla.biketeam.service.auth;

import info.tomacla.biketeam.service.mail.MailSenderService;
import info.tomacla.biketeam.service.url.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Emails transactionnels lies a l'authentification (verification d'adresse, mot de passe).
 * <p>
 * Le HTML est construit en StringBuilder, comme les autres emails de l'application :
 * aucun rendu FreeMarker n'est introduit ici.
 */
@Service
public class AuthMailService {

    private static final Logger log = LoggerFactory.getLogger(AuthMailService.class);

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private UrlService urlService;

    public void sendEmailVerification(String to, String clearToken) {

        final String url = urlService.getUrlWithSuffix("/verify-email?code=" + clearToken);

        if (!checkSmtp(to, url)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Confirmez votre adresse email").append("</h5>");
        sb.append("<p>").append("Merci de confirmer votre adresse email en cliquant sur le lien ci dessous. Ce lien est valable 24 heures.").append("</p>");
        sb.append("<p>").append(getHtmlLink(url)).append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(to), "Confirmez votre adresse email", sb.toString(), null);

    }

    public void sendPasswordReset(String to, String clearToken) {

        final String url = urlService.getUrlWithSuffix("/reset-password?code=" + clearToken);

        if (!checkSmtp(to, url)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Réinitialisation de votre mot de passe").append("</h5>");
        sb.append("<p>").append("Vous avez demandé la réinitialisation de votre mot de passe. Ce lien est valable 1 heure.").append("</p>");
        sb.append("<p>").append(getHtmlLink(url)).append("</p>");
        sb.append("<p>").append("Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.").append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(to), "Réinitialisation de votre mot de passe", sb.toString(), null);

    }

    public void sendDefinePassword(String to, String clearToken) {

        final String url = urlService.getUrlWithSuffix("/reset-password?code=" + clearToken);

        if (!checkSmtp(to, url)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Définissez votre mot de passe").append("</h5>");
        sb.append("<p>").append("Un compte existe déjà avec cette adresse. Cliquez sur le lien ci dessous pour définir votre mot de passe et confirmer votre adresse.").append("</p>");
        sb.append("<p>").append(getHtmlLink(url)).append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(to), "Définissez votre mot de passe", sb.toString(), null);

    }

    public void sendAccountAlreadyExists(String to) {

        if (!mailSenderService.isSmtpConfigured()) {
            log.warn("SMTP non configuré - email 'compte existant' non envoyé à {}", to);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Tentative de création de compte").append("</h5>");
        sb.append("<p>").append("Quelqu'un a tenté de créer un compte avec votre adresse email. Si c'était vous, connectez-vous ou utilisez le lien \"mot de passe oublié\".").append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(to), "Tentative de création de compte", sb.toString(), null);

    }

    public void sendEmailChangeAlert(String oldEmail, String newEmail) {

        if (!mailSenderService.isSmtpConfigured()) {
            log.warn("SMTP non configuré - alerte de changement d'adresse non envoyée à {} (nouvelle adresse {})", oldEmail, newEmail);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Votre adresse email a été modifiée").append("</h5>");
        sb.append("<p>").append("Une demande de changement d'adresse email vers ").append(newEmail).append(" a été enregistrée sur votre compte.").append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(oldEmail), "Votre adresse email a été modifiée", sb.toString(), null);

    }

    /**
     * En l'absence de SMTP, le lien est journalise en clair : indispensable en developpement.
     */
    private boolean checkSmtp(String to, String url) {
        if (!mailSenderService.isSmtpConfigured()) {
            log.warn("SMTP non configuré - lien d'authentification pour {} : {}", to, url);
            return false;
        }
        return true;
    }

    private String getHtmlLink(String href) {
        return "<a href=\"" + href + "\">" + href + "</a>";
    }

}
