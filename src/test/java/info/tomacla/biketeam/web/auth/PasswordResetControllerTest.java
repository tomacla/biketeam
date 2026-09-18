package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.session.UserSessionService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.BotProtectionService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

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
 * Mot de passe oublie et reinitialisation.
 * <p>
 * Deux invariants sont verrouilles ici : la reponse de /forgot-password est strictement
 * inconditionnelle (aucune enumeration de comptes), et une reinitialisation reussie coupe TOUS les
 * acces existants (graine remember-me et sessions HTTP persistees).
 */
public class PasswordResetControllerTest {

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private AuthMailService authMailService;
    private RateLimitService rateLimitService;
    private BotProtectionService botProtectionService;
    private UserSessionService userSessionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        authMailService = mock(AuthMailService.class);
        rateLimitService = mock(RateLimitService.class);
        botProtectionService = mock(BotProtectionService.class);
        userSessionService = mock(UserSessionService.class);

        PasswordResetController controller = new PasswordResetController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "authMailService", authMailService);
        ReflectionTestUtils.setField(controller, "rateLimitService", rateLimitService);
        ReflectionTestUtils.setField(controller, "botProtectionService", botProtectionService);
        ReflectionTestUtils.setField(controller, "userSessionService", userSessionService);
        ReflectionTestUtils.setField(controller, "maxForgotPasswordPerHour", 3);

        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(true);
        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HUMAN);
        when(rateLimitService.clientKey(any(), anyString())).thenAnswer(i -> i.getArgument(1) + ":1.2.3.4");
        when(userAuthTokenService.getPasswordResetValidity()).thenReturn(Duration.ofHours(1));
        when(userAuthTokenService.create(any(), any(), any(), any())).thenReturn("clear-token");
        when(userService.getByEmail(anyString())).thenReturn(Optional.empty());
        when(userService.isEmailAvailable(anyString(), anyString())).thenReturn(true);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    private User user(String id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private UserAuthToken token(String userId, String targetEmail) {
        UserAuthToken token = new UserAuthToken();
        token.setId("token-1");
        token.setUserId(userId);
        token.setType(UserAuthTokenType.PASSWORD_RESET);
        token.setTargetEmail(targetEmail);
        return token;
    }

    // --- /forgot-password ---

    @Test
    public void testForgotPasswordPageExposesAnEmptyForm() throws Exception {

        mockMvc.perform(get("/forgot-password"))
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attributeExists("formdata"));

    }

    @Test
    public void testForgotPasswordSendsTheResetLink() throws Exception {

        when(userService.getByEmail("jean@example.com")).thenReturn(Optional.of(user("user-1", "jean@example.com")));

        mockMvc.perform(post("/forgot-password").param("email", "Jean@Example.com"))
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(flash().attributeExists("infos"));

        verify(userAuthTokenService).create("user-1", UserAuthTokenType.PASSWORD_RESET,
                "jean@example.com", Duration.ofHours(1));
        verify(authMailService).sendPasswordReset("jean@example.com", "clear-token");

    }

    /**
     * Adresse inconnue : aucun mail, mais reponse strictement identique.
     */
    @Test
    public void testForgotPasswordOnUnknownAddressAnswersTheSame() throws Exception {

        mockMvc.perform(post("/forgot-password").param("email", "unknown@example.com"))
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(flash().attributeExists("infos"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testForgotPasswordRejectsInvalidAddress() throws Exception {

        mockMvc.perform(post("/forgot-password").param("email", "not-an-email"))
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attributeExists("errors"));

        verifyNoInteractions(rateLimitService);
        verifyNoInteractions(authMailService);

    }

    @Test
    public void testForgotPasswordRateLimitedAnswersTheSame() throws Exception {

        when(userService.getByEmail("jean@example.com")).thenReturn(Optional.of(user("user-1", "jean@example.com")));
        when(rateLimitService.tryAcquire(eq("forgot-password:jean@example.com"), anyInt())).thenReturn(false);

        mockMvc.perform(post("/forgot-password").param("email", "jean@example.com"))
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(flash().attributeExists("infos"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testBotControlsReceiveTheSubmittedFields() throws Exception {

        mockMvc.perform(post("/forgot-password")
                .param("email", "jean@example.com")
                .param("altcha", "payload")
                .param("website", "")
                .param("formStamp", "stamp"));

        verify(botProtectionService).check("payload", "", "stamp");

    }

    /**
     * Champ piege rempli : reponse inconditionnelle habituelle, sans aucun envoi.
     */
    @Test
    public void testHoneypotAnswersTheSameWithoutSending() throws Exception {

        when(userService.getByEmail("jean@example.com")).thenReturn(Optional.of(user("user-1", "jean@example.com")));
        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HONEYPOT);

        mockMvc.perform(post("/forgot-password").param("email", "jean@example.com"))
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(flash().attributeExists("infos"));

        verifyNoInteractions(authMailService);
        verify(userAuthTokenService, never()).create(any(), any(), any(), any());

    }

    @Test
    public void testFailedBotControlRedisplaysTheForm() throws Exception {

        when(userService.getByEmail("jean@example.com")).thenReturn(Optional.of(user("user-1", "jean@example.com")));

        for (BotProtectionService.Verdict verdict : List.of(BotProtectionService.Verdict.TOO_FAST,
                BotProtectionService.Verdict.CHALLENGE_FAILED)) {

            when(botProtectionService.check(any(), any(), any())).thenReturn(verdict);

            mockMvc.perform(post("/forgot-password").param("email", "jean@example.com"))
                    .andExpect(view().name("forgot_password"))
                    .andExpect(model().attribute("errors", List.of(BotProtectionService.errorMessage(verdict))));

        }

        verifyNoInteractions(authMailService);
        verify(rateLimitService, never()).tryAcquire(any(), anyInt());

    }

    /**
     * Le & non court-circuitant est volontaire : saturer une seule adresse ne doit pas permettre
     * d'echapper au comptage par origine de requete.
     */
    @Test
    public void testBothCountersAreAlwaysIncremented() throws Exception {

        when(rateLimitService.tryAcquire(eq("forgot-password:jean@example.com"), anyInt())).thenReturn(false);

        mockMvc.perform(post("/forgot-password").param("email", "jean@example.com"));

        verify(rateLimitService).tryAcquire("forgot-password:jean@example.com", 3);
        verify(rateLimitService).tryAcquire("forgot-password:1.2.3.4", 15);

    }

    // --- GET /reset-password ---

    @Test
    public void testResetPasswordPageRefusesAnUnusableCode() throws Exception {

        when(userAuthTokenService.isUsable(any(), any())).thenReturn(false);

        mockMvc.perform(get("/reset-password").param("code", "dead-code"))
                .andExpect(view().name("redirect:/forgot-password"))
                .andExpect(flash().attributeExists("errors"));

    }

    @Test
    public void testResetPasswordPageKeepsTheCodeInTheForm() throws Exception {

        when(userAuthTokenService.isUsable("live-code", UserAuthTokenType.PASSWORD_RESET)).thenReturn(true);

        mockMvc.perform(get("/reset-password").param("code", "live-code"))
                .andExpect(view().name("reset_password"))
                .andExpect(model().attribute("formdata",
                        org.hamcrest.Matchers.hasProperty("code", org.hamcrest.Matchers.equalTo("live-code"))));

        // afficher le formulaire ne doit jamais consommer le lien
        verify(userAuthTokenService, never()).consume(any(), any());

    }

    // --- POST /reset-password ---

    /**
     * Une faute de frappe dans la confirmation ne doit pas bruler le lien.
     */
    @Test
    public void testMismatchDoesNotBurnTheToken() throws Exception {

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse124"))
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(any(), any());

    }

    @Test
    public void testInvalidTokenIsRefused() throws Exception {

        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/reset-password")
                        .param("code", "dead-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    @Test
    public void testTokenOfAnUnknownUserIsRefused() throws Exception {

        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.of(token("gone", "jean@example.com")));
        when(userService.get("gone")).thenReturn(Optional.empty());

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    /**
     * Reinitialisation nominale : l'adresse devient verifiee, le mot de passe est pose, et TOUS
     * les acces preexistants sont coupes (cookies remember-me et sessions HTTP).
     */
    @Test
    public void testSuccessfulResetCutsEveryExistingAccess() throws Exception {

        User user = user("user-1", "jean@example.com");
        when(userAuthTokenService.consume("live-code", UserAuthTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("infos"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertTrue(captor.getValue().isEmailVerified(), "la reception du mail prouve le controle de la boite");

        verify(userService).setPassword("user-1", "motdepasse123");
        verify(userService).rotateAuthTokenSeed("user-1");
        verify(userSessionService).invalidateAll("user-1");
        verify(userAuthTokenService).invalidateAll("user-1", UserAuthTokenType.PASSWORD_RESET);

    }

    /**
     * Token sans adresse cible : l'adresse deja portee par le compte est utilisee.
     */
    @Test
    public void testTokenWithoutTargetEmailFallsBackToTheAccountAddress() throws Exception {

        User user = user("user-1", "jean@example.com");
        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.of(token("user-1", null)));
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/login"));

        assertEquals("jean@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());

    }

    /**
     * L'adresse a ete prise entre temps : elle n'est pas volee, mais le mot de passe est tout de
     * meme pose (le lien a bien prouve le controle de la boite au moment de l'emission).
     */
    @Test
    public void testAddressTakenMeanwhileIsNotStolen() throws Exception {

        User user = user("user-1", "jean@example.com");
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        when(userService.isEmailAvailable("jean@example.com", "user-1")).thenReturn(false);

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/login"));

        verify(userService, never()).save(any(User.class));
        verify(userService).setPassword("user-1", "motdepasse123");

    }

    /**
     * Mot de passe refuse par une regle dependant du compte (il contient l'adresse) : le lien
     * vient d'etre consomme, un nouveau doit etre emis immediatement.
     */
    @Test
    public void testPasswordContainingTheEmailReissuesANewLink() throws Exception {

        User user = user("user-1", "jeandupont@example.com");
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jeandupont@example.com")));
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/reset-password")
                        .param("code", "live-code")
                        .param("password", "jeandupont2024")
                        .param("passwordConfirm", "jeandupont2024"))
                .andExpect(view().name("reset_password"))
                .andExpect(model().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());
        verify(userAuthTokenService).create("user-1", UserAuthTokenType.PASSWORD_RESET,
                "jeandupont@example.com", Duration.ofHours(1));
        verify(authMailService).sendPasswordReset(eq("jeandupont@example.com"), anyString());

    }

}
