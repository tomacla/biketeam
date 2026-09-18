package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.password.LoginFailureHandler;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.BotProtectionService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import info.tomacla.biketeam.web.ControllerTestSupport;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Inscription par email et mot de passe.
 * <p>
 * Invariant principal verrouille ici : les trois issues (adresse libre, compte complet existant,
 * compte existant sans mot de passe) produisent exactement la meme reponse. Aucune enumeration de
 * comptes ne doit etre possible depuis cette route, et le mot de passe saisi ne doit jamais etre
 * pose sur un compte preexistant.
 */
public class RegistrationControllerTest {

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private AuthMailService authMailService;
    private RateLimitService rateLimitService;
    private BotProtectionService botProtectionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        authMailService = mock(AuthMailService.class);
        rateLimitService = mock(RateLimitService.class);
        botProtectionService = mock(BotProtectionService.class);

        RegistrationController controller = new RegistrationController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "authMailService", authMailService);
        ReflectionTestUtils.setField(controller, "rateLimitService", rateLimitService);
        ReflectionTestUtils.setField(controller, "botProtectionService", botProtectionService);
        ReflectionTestUtils.setField(controller, "maxRegisterPerHour", 5);

        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(true);
        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HUMAN);
        when(botProtectionService.issueFormStamp()).thenReturn("stamp");
        when(rateLimitService.clientKey(any(), anyString())).thenAnswer(i -> i.getArgument(1) + ":1.2.3.4");
        when(userAuthTokenService.getEmailVerificationValidity()).thenReturn(Duration.ofHours(24));
        when(userAuthTokenService.getPasswordResetValidity()).thenReturn(Duration.ofHours(1));
        when(userAuthTokenService.create(any(), any(), any(), any())).thenReturn("clear-token");
        when(userService.getByEmail(anyString())).thenReturn(Optional.empty());
        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId("new-user-id");
            }
            return user;
        });

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    private User existingUser(String id, String email, boolean verified, String passwordHash) {
        User user = new User();
        user.setId(id);
        if (verified) {
            user.setVerifiedEmail(email);
        } else {
            user.setEmail(email);
        }
        user.setPasswordHash(passwordHash);
        return user;
    }

    @Test
    public void testRegisterPageExposesAnEmptyForm() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("formdata"))
                .andExpect(model().attribute("formStamp", "stamp"));

    }

    @Test
    public void testRegisterCreatesAccountAndSendsVerification() throws Exception {

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "Jean@Example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(request().sessionAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertEquals("jean@example.com", captor.getValue().getEmail());
        assertFalse(captor.getValue().isEmailVerified(), "une adresse saisie n'est jamais verifiee d'emblee");

        verify(userService).setPassword("new-user-id", "motdepasse123");
        verify(userAuthTokenService).create("new-user-id", UserAuthTokenType.EMAIL_VERIFICATION,
                "jean@example.com", Duration.ofHours(24));
        verify(authMailService).sendEmailVerification("jean@example.com", "clear-token");

    }

    /**
     * Compte deja complet : on previent le titulaire et on ne modifie RIEN. Poser le mot de passe
     * saisi serait une prise de controle immediate.
     */
    @Test
    public void testRegisterOnExistingCompleteAccountOnlyWarnsTheOwner() throws Exception {

        when(userService.getByEmail("jean@example.com"))
                .thenReturn(Optional.of(existingUser("user-1", "jean@example.com", true, "$2a$10$hash")));

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/register/pending"));

        verify(authMailService).sendAccountAlreadyExists("jean@example.com");
        verify(userService, never()).save(any(User.class));
        verify(userService, never()).setPassword(anyString(), anyString());
        verify(userAuthTokenService, never()).create(any(), any(), any(), any());

    }

    /**
     * Compte existant sans mot de passe (compte Strava migre) : un lien de definition de mot de
     * passe est envoye, le compte est conserve avec ses equipes et son historique.
     */
    @Test
    public void testRegisterOnExistingAccountWithoutPasswordSendsDefinePasswordLink() throws Exception {

        when(userService.getByEmail("jean@example.com"))
                .thenReturn(Optional.of(existingUser("user-1", "jean@example.com", false, null)));

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/register/pending"));

        verify(userAuthTokenService).create("user-1", UserAuthTokenType.PASSWORD_RESET,
                "jean@example.com", Duration.ofHours(1));
        verify(authMailService).sendDefinePassword("jean@example.com", "clear-token");
        verify(userService, never()).setPassword(anyString(), anyString());

    }

    /**
     * Course entre deux inscriptions sur la meme adresse : l'index unique a tranche, la reponse
     * reste identique aux autres cas.
     */
    @Test
    public void testConcurrentRegistrationKeepsTheSameAnswer() throws Exception {

        when(userService.save(any(User.class))).thenThrow(new DataIntegrityViolationException("unique"));

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(request().sessionAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"));

    }

    @Test
    public void testInvalidEmailRedisplaysTheFormWithoutThePassword() throws Exception {

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "not-an-email")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("formdata",
                        hasProperty("password", "")))
                .andExpect(model().attribute("formdata",
                        hasProperty("firstName", "Jean")));

        verifyNoInteractions(authMailService);
        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testPasswordMismatchRedisplaysTheForm() throws Exception {

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse124"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errors"));

        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testTooShortPasswordRedisplaysTheForm() throws Exception {

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "court")
                        .param("passwordConfirm", "court"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testRateLimitedRegistrationSendsNoMail() throws Exception {

        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errors"));

        verifyNoInteractions(authMailService);
        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testBotControlsReceiveTheSubmittedFields() throws Exception {

        mockMvc.perform(post("/register")
                .param("firstName", "Jean")
                .param("lastName", "Dupont")
                .param("email", "jean@example.com")
                .param("password", "motdepasse123")
                .param("passwordConfirm", "motdepasse123")
                .param("altcha", "payload")
                .param("website", "")
                .param("formStamp", "stamp"));

        verify(botProtectionService).check("payload", "", "stamp");

    }

    /**
     * Champ piege rempli : le robot recoit la reponse d'une inscription reussie, sans qu'aucun
     * compte ni mail ne soit cree.
     */
    @Test
    public void testHoneypotFakesASuccessfulRegistration() throws Exception {

        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HONEYPOT);

        mockMvc.perform(post("/register")
                        .param("firstName", "Jean")
                        .param("lastName", "Dupont")
                        .param("email", "jean@example.com")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123")
                        .param("website", "https://spam.example"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(request().sessionAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"));

        verifyNoInteractions(authMailService);
        verify(userService, never()).save(any(User.class));

    }

    /**
     * Envoi trop rapide ou defi ALTCHA invalide : peut toucher un humain, qui voit donc un message
     * et un formulaire pret a etre renvoye.
     */
    @Test
    public void testFailedBotControlRedisplaysTheFormWithANewStamp() throws Exception {

        for (BotProtectionService.Verdict verdict : List.of(BotProtectionService.Verdict.TOO_FAST,
                BotProtectionService.Verdict.CHALLENGE_FAILED)) {

            when(botProtectionService.check(any(), any(), any())).thenReturn(verdict);

            mockMvc.perform(post("/register")
                            .param("firstName", "Jean")
                            .param("lastName", "Dupont")
                            .param("email", "jean@example.com")
                            .param("password", "motdepasse123")
                            .param("passwordConfirm", "motdepasse123"))
                    .andExpect(view().name("register"))
                    .andExpect(model().attribute("errors", List.of(BotProtectionService.errorMessage(verdict))))
                    .andExpect(model().attribute("formStamp", "stamp"))
                    .andExpect(model().attribute("formdata", hasProperty("password", "")));

        }

        verifyNoInteractions(authMailService);
        verify(userService, never()).save(any(User.class));
        verify(rateLimitService, never()).tryAcquire(any(), anyInt());

    }

    // --- /register/pending ---

    @Test
    public void testPendingPageRequiresAPendingEmail() throws Exception {

        mockMvc.perform(get("/register/pending"))
                .andExpect(view().name("redirect:/login"));

    }

    @Test
    public void testPendingPageShowsThePendingEmail() throws Exception {

        mockMvc.perform(get("/register/pending")
                        .sessionAttr(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"))
                .andExpect(view().name("register_pending"))
                .andExpect(model().attribute("pendingEmail", "jean@example.com"));

    }

    // --- /register/resend ---

    @Test
    public void testResendRequiresAPendingEmail() throws Exception {

        mockMvc.perform(post("/register/resend"))
                .andExpect(view().name("redirect:/login"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testResendSendsANewVerificationLink() throws Exception {

        when(userService.getByEmail("jean@example.com"))
                .thenReturn(Optional.of(existingUser("user-1", "jean@example.com", false, null)));

        mockMvc.perform(post("/register/resend")
                        .sessionAttr(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(flash().attributeExists("infos"));

        verify(authMailService).sendEmailVerification("jean@example.com", "clear-token");

    }

    /**
     * Compte deja verifie : aucun mail, mais le message de confirmation reste inconditionnel.
     */
    @Test
    public void testResendOnVerifiedAccountSendsNothingButAnswersTheSame() throws Exception {

        when(userService.getByEmail("jean@example.com"))
                .thenReturn(Optional.of(existingUser("user-1", "jean@example.com", true, "$2a$10$hash")));

        mockMvc.perform(post("/register/resend")
                        .sessionAttr(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(flash().attributeExists("infos"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testResendIsRateLimited() throws Exception {

        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/register/resend")
                        .sessionAttr(LoginFailureHandler.REGISTER_PENDING_EMAIL, "jean@example.com"))
                .andExpect(view().name("redirect:/register/pending"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    private static org.hamcrest.Matcher<Object> hasProperty(String property, String expected) {
        return org.hamcrest.Matchers.hasProperty(property, org.hamcrest.Matchers.equalTo(expected));
    }

}
