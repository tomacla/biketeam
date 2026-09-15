package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Completion forcee d'un compte incomplet.
 * <p>
 * Deux invariants critiques : la pose du premier mot de passe est interdite tant que l'adresse
 * n'est pas verifiee (sinon la session d'un compte Strava suffirait a se l'approprier), et une
 * completion reussie doit rafraichir le principal ET marquer la session complete dans la meme
 * requete, faute de quoi l'utilisateur reste prisonnier de la boucle de redirection.
 */
public class AccountCompletionControllerTest {

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private AuthMailService authMailService;
    private SecurityContextService securityContextService;
    private AccountCompletionService accountCompletionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        authMailService = mock(AuthMailService.class);
        securityContextService = mock(SecurityContextService.class);
        accountCompletionService = mock(AccountCompletionService.class);

        AccountCompletionController controller = new AccountCompletionController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "authMailService", authMailService);
        ReflectionTestUtils.setField(controller, "securityContextService", securityContextService);
        ReflectionTestUtils.setField(controller, "accountCompletionService", accountCompletionService);

        when(userAuthTokenService.getEmailVerificationValidity()).thenReturn(Duration.ofHours(24));
        when(userAuthTokenService.create(any(), any(), any(), any())).thenReturn("clear-token");
        when(userAuthTokenService.getPendingTargetEmail(anyString(), any())).thenReturn(Optional.empty());
        when(userAuthTokenService.isThrottled(anyString(), any(), anyInt())).thenReturn(false);
        when(userService.isEmailAvailable(anyString(), anyString())).thenReturn(true);

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    private User stravaUser() {
        User user = new User();
        user.setId("user-1");
        user.setStravaId(42L);
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        return user;
    }

    private User verifiedUser() {
        User user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("jean@example.com");
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        return user;
    }

    // --- GET /account/complete ---

    @Test
    public void testPageRequiresAConnectedUser() throws Exception {

        mockMvc.perform(get("/account/complete"))
                .andExpect(view().name("redirect:/"));

    }

    /**
     * Compte devenu complet ailleurs : la session est marquee complete, ce qui est la sortie de
     * secours de la boucle de redirection.
     */
    @Test
    public void testAlreadyCompleteAccountMarksTheSessionAndLeaves() throws Exception {

        User user = verifiedUser();
        user.setPasswordHash("$2a$10$hash");

        mockMvc.perform(get("/account/complete").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/"));

        verify(accountCompletionService).markComplete(any());

    }

    @Test
    public void testIncompleteAccountSeesTheCompletionPage() throws Exception {

        User user = stravaUser();
        when(userAuthTokenService.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of("pending@example.com"));

        mockMvc.perform(get("/account/complete").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("account_complete"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("pendingEmail", "pending@example.com"))
                .andExpect(model().attributeExists("formdata"));

        verify(accountCompletionService, never()).markComplete(any());

    }

    // --- POST /account/complete/email ---

    @Test
    public void testCompleteEmailRequiresAConnectedUser() throws Exception {

        mockMvc.perform(post("/account/complete/email").param("email", "jean@example.com"))
                .andExpect(view().name("redirect:/"));

    }

    /**
     * Changer une adresse DEJA prouvee revient a changer l'identite de connexion : un simple
     * cookie remember-me ne doit pas l'autoriser.
     */
    @Test
    public void testChangingAProvenAddressFromARememberMeSessionIsRefused() throws Exception {

        User user = verifiedUser();

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "autre@example.com")
                        .principal(ControllerTestSupport.rememberMeAuthentication(user)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    /**
     * La saisie d'une PREMIERE adresse reste possible depuis une session remember-me : c'est le
     * parcours nominal d'un compte Strava a completer.
     */
    @Test
    public void testFirstAddressIsAllowedFromARememberMeSession() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "jean@example.com")
                        .principal(ControllerTestSupport.rememberMeAuthentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("infos"));

        verify(authMailService).sendEmailVerification("jean@example.com", "clear-token");

    }

    @Test
    public void testInvalidAddressIsRefused() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "not-an-email")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testAddressUsedByAnotherAccountIsRefused() throws Exception {

        User user = stravaUser();
        when(userService.isEmailAvailable("taken@example.com", "user-1")).thenReturn(false);

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "taken@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testThrottledRequestSendsNoMail() throws Exception {

        User user = stravaUser();
        when(userAuthTokenService.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5)).thenReturn(true);

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "jean@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    /**
     * L'ancienne adresse est toujours prevenue : une adresse n'est jamais remplacee en silence.
     */
    @Test
    public void testOldAddressIsAlerted() throws Exception {

        User user = new User();
        user.setId("user-1");
        user.setEmail("ancienne@example.com");
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "nouvelle@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"));

        verify(authMailService).sendEmailVerification("nouvelle@example.com", "clear-token");
        verify(authMailService).sendEmailChangeAlert("ancienne@example.com", "nouvelle@example.com");

        // l'adresse du compte n'est jamais modifiee avant confirmation
        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testSameAddressTriggersNoChangeAlert() throws Exception {

        User user = new User();
        user.setId("user-1");
        user.setEmail("jean@example.com");
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/account/complete/email")
                        .param("email", "jean@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"));

        verify(authMailService, never()).sendEmailChangeAlert(anyString(), anyString());

    }

    // --- POST /account/complete/resend ---

    @Test
    public void testResendWithoutAnyAddressIsRefused() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/account/complete/resend")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testResendUsesThePendingAddress() throws Exception {

        User user = stravaUser();
        when(userAuthTokenService.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of("pending@example.com"));

        mockMvc.perform(post("/account/complete/resend")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("infos"));

        verify(authMailService).sendEmailVerification("pending@example.com", "clear-token");

    }

    @Test
    public void testResendIsThrottled() throws Exception {

        User user = stravaUser();
        when(userAuthTokenService.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of("pending@example.com"));
        when(userAuthTokenService.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5)).thenReturn(true);

        mockMvc.perform(post("/account/complete/resend")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    // --- POST /account/complete/password ---

    @Test
    public void testPasswordRequiresAVerifiedAddress() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/account/complete/password")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    @Test
    public void testPasswordPolicyIsEnforced() throws Exception {

        User user = verifiedUser();

        mockMvc.perform(post("/account/complete/password")
                        .param("password", "court")
                        .param("passwordConfirm", "court")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    /**
     * Completion reussie : le principal est reconstruit et la session marquee complete dans la
     * meme requete, sinon l'utilisateur reste bloque par AccountCompletionFilter.
     */
    @Test
    public void testSuccessfulCompletionRefreshesPrincipalAndMarksSession() throws Exception {

        User user = verifiedUser();

        mockMvc.perform(post("/account/complete/password")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("infos"));

        verify(userService).setPassword("user-1", "motdepasse123");
        verify(securityContextService).refreshCurrentUser(eq(user), any(), any());
        verify(accountCompletionService).markComplete(any());

    }

    @Test
    public void testPasswordRequiresAConnectedUser() throws Exception {

        mockMvc.perform(post("/account/complete/password")
                        .param("password", "motdepasse123")
                        .param("passwordConfirm", "motdepasse123"))
                .andExpect(view().name("redirect:/"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

}
