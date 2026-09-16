package info.tomacla.biketeam.security.password;

import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Traitement des echecs de connexion par mot de passe.
 * <p>
 * Le cas "email non verifie" n'est atteint qu'apres validation du mot de passe : on peut donc
 * sans risque de divulgation renvoyer un nouveau lien de confirmation a l'adresse du compte.
 * <p>
 * L'envoi est neanmoins limite en debit, par compte ET par origine : POST /login est une route non
 * authentifiee declenchant un mail, et ce chemin ne passe pas par le comptage d'echecs (le mot de
 * passe est bon). Sans limitation, rejouer la requete produirait un mail sortant par requete, donc
 * du harcelement par mail et la perte de reputation SMTP de l'instance. La redirection reste
 * inconditionnelle : la limitation ne doit rien divulguer.
 */
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public static final String REGISTER_PENDING_EMAIL = "REGISTER_PENDING_EMAIL";

    private static final int MAX_EMAIL_VERIFICATIONS_PER_HOUR = 5;

    private static final int MAX_EMAIL_VERIFICATIONS_PER_HOUR_AND_IP = 10;

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    @Autowired
    private UserAuthTokenService userAuthTokenService;

    @Autowired
    private AuthMailService authMailService;

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof EmailNotVerifiedException emailNotVerified) {

            final String email = emailNotVerified.getEmail();

            final boolean allowed =
                    !userAuthTokenService.isThrottled(emailNotVerified.getUserId(),
                            UserAuthTokenType.EMAIL_VERIFICATION, MAX_EMAIL_VERIFICATIONS_PER_HOUR)
                            & rateLimitService.tryAcquire(
                            rateLimitService.clientKey(request, "login-email-verification"),
                            MAX_EMAIL_VERIFICATIONS_PER_HOUR_AND_IP);

            if (allowed) {
                try {
                    final String clearToken = userAuthTokenService.create(
                            emailNotVerified.getUserId(),
                            UserAuthTokenType.EMAIL_VERIFICATION,
                            email,
                            userAuthTokenService.getEmailVerificationValidity());
                    authMailService.sendEmailVerification(email, clearToken);
                } catch (Exception e) {
                    log.error("Unable to send email verification to user {}", emailNotVerified.getUserId(), e);
                }
            } else {
                log.warn("Email verification resend throttled for user {}", emailNotVerified.getUserId());
            }

            request.getSession().setAttribute(REGISTER_PENDING_EMAIL, email);

            getRedirectStrategy().sendRedirect(request, response, "/register/pending");
            return;

        }

        if (exception instanceof LockedException) {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=locked");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/login?error");

    }

}
