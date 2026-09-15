package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Gestion des moyens de connexion depuis /users/me.
 * <p>
 * Regle centrale : toute operation touchant un MOYEN DE CONNEXION (mot de passe, adresse email,
 * deliaison d'une identite externe) exige une preuve de possession, et ne doit jamais laisser le
 * compte sans aucun moyen de connexion.
 */
public class UserControllerTest {

    private static final String HASH = "$2a$10$hash";

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private AuthMailService authMailService;
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        authMailService = mock(AuthMailService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userRoleService", mock(UserRoleService.class));
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "authMailService", authMailService);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);

        when(userAuthTokenService.getEmailVerificationValidity()).thenReturn(Duration.ofHours(24));
        when(userAuthTokenService.create(any(), any(), any(), any())).thenReturn("clear-token");
        when(userAuthTokenService.getPendingTargetEmail(anyString(), any())).thenReturn(Optional.empty());
        when(userAuthTokenService.isThrottled(anyString(), any(), anyInt())).thenReturn(false);
        when(userService.isEmailAvailable(anyString(), anyString())).thenReturn(true);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.matches("bonmotdepasse", HASH)).thenReturn(true);

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    /**
     * Compte avec email verifie et mot de passe.
     */
    private User fullUser() {
        User user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("jean@example.com");
        user.setPasswordHash(HASH);
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        return user;
    }

    /**
     * Compte Strava nu : ni email verifie ni mot de passe.
     */
    private User stravaUser() {
        User user = new User();
        user.setId("user-1");
        user.setStravaId(42L);
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        return user;
    }

    // --- GET /users/me ---

    @Test
    public void testProfileRequiresAConnectedUser() throws Exception {

        mockMvc.perform(get("/users/me"))
                .andExpect(view().name("redirect:/"));

    }

    @Test
    public void testProfileExposesThePendingAddress() throws Exception {

        User user = fullUser();
        when(userAuthTokenService.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of("nouvelle@example.com"));

        mockMvc.perform(get("/users/me").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("user"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("pendingEmail", "nouvelle@example.com"))
                .andExpect(model().attributeExists("formdata"));

    }

    // --- POST /users/me ---

    /**
     * Les preferences de notification ne doivent toucher ni l'adresse ni l'identite Strava.
     */
    @Test
    public void testUpdateUserOnlyChangesNotificationPreferences() throws Exception {

        User user = fullUser();
        user.setEmailPublishRides(true);

        mockMvc.perform(post("/users/me")
                        // cases a cocher : seule la valeur "on" vaut vrai, l'absence vaut faux
                        .param("emailPublishTrips", "on")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("user"));

        assertFalse(user.isEmailPublishRides());
        assertTrue(user.isEmailPublishTrips());
        assertEquals("jean@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());
        verify(userService).save(user);

    }

    // --- POST /users/me/password ---

    @Test
    public void testPasswordChangeRequiresTheCurrentPassword() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/password")
                        .param("currentPassword", "mauvais-mot-de-passe")
                        .param("password", "nouveaupass123")
                        .param("passwordConfirm", "nouveaupass123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    @Test
    public void testPasswordChangeWithTheCorrectCurrentPassword() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/password")
                        .param("currentPassword", "bonmotdepasse")
                        .param("password", "nouveaupass123")
                        .param("passwordConfirm", "nouveaupass123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(userService).setPassword("user-1", "nouveaupass123");

    }

    @Test
    public void testPasswordPolicyIsEnforcedOnChange() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/password")
                        .param("currentPassword", "bonmotdepasse")
                        .param("password", "court")
                        .param("passwordConfirm", "court")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    /**
     * Premier mot de passe : l'adresse doit avoir ete prouvee au prealable.
     */
    @Test
    public void testFirstPasswordRequiresAVerifiedAddress() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/users/me/password")
                        .param("password", "nouveaupass123")
                        .param("passwordConfirm", "nouveaupass123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    /**
     * Aucun mot de passe actuel a opposer : un cookie remember-me vole ne doit pas suffire a poser
     * le premier mot de passe du compte.
     */
    @Test
    public void testFirstPasswordIsRefusedFromARememberMeSession() throws Exception {

        User user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("jean@example.com");
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/me/password")
                        .param("password", "nouveaupass123")
                        .param("passwordConfirm", "nouveaupass123")
                        .principal(ControllerTestSupport.rememberMeAuthentication(user)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).setPassword(anyString(), anyString());

    }

    @Test
    public void testFirstPasswordIsAcceptedFromAFreshSession() throws Exception {

        User user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("jean@example.com");
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/me/password")
                        .param("password", "nouveaupass123")
                        .param("passwordConfirm", "nouveaupass123")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(userService).setPassword("user-1", "nouveaupass123");

    }

    // --- POST /users/me/email ---

    /**
     * L'adresse est une identite de connexion : la changer exige la preuve de possession du
     * compte, exactement comme un changement de mot de passe.
     */
    @Test
    public void testEmailChangeRequiresTheCurrentPassword() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "nouvelle@example.com")
                        .param("currentPassword", "mauvais-mot-de-passe")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testEmailChangeWithoutCurrentPasswordIsRefused() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "nouvelle@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    /**
     * Nominal : l'adresse du compte n'est PAS modifiee, seul un lien de verification part.
     */
    @Test
    public void testEmailChangeOnlySendsAVerificationLink() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "Nouvelle@Example.com")
                        .param("currentPassword", "bonmotdepasse")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(userAuthTokenService).create("user-1", UserAuthTokenType.EMAIL_VERIFICATION,
                "nouvelle@example.com", Duration.ofHours(24));
        verify(authMailService).sendEmailVerification("nouvelle@example.com", "clear-token");
        verify(authMailService).sendEmailChangeAlert("jean@example.com", "nouvelle@example.com");

        assertEquals("jean@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());
        verify(userService, never()).save(any(User.class));

    }

    /**
     * Sans mot de passe sur le compte, une authentification fraiche est exigee.
     */
    @Test
    public void testEmailChangeIsRefusedFromARememberMeSessionWithoutPassword() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "nouvelle@example.com")
                        .principal(ControllerTestSupport.rememberMeAuthentication(user)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testEmailChangeIsAllowedWithoutPasswordFromAFreshSession() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "nouvelle@example.com")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(authMailService).sendEmailVerification("nouvelle@example.com", "clear-token");

    }

    @Test
    public void testAlreadyVerifiedAddressSendsNothing() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "jean@example.com")
                        .param("currentPassword", "bonmotdepasse")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testAddressUsedByAnotherAccountIsRefused() throws Exception {

        User user = fullUser();
        when(userService.isEmailAvailable("taken@example.com", "user-1")).thenReturn(false);

        mockMvc.perform(post("/users/me/email")
                        .param("email", "taken@example.com")
                        .param("currentPassword", "bonmotdepasse")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testInvalidAddressIsRefused() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/email")
                        .param("email", "not-an-email")
                        .param("currentPassword", "bonmotdepasse")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testEmailChangeIsThrottled() throws Exception {

        User user = fullUser();
        when(userAuthTokenService.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5)).thenReturn(true);

        mockMvc.perform(post("/users/me/email")
                        .param("email", "nouvelle@example.com")
                        .param("currentPassword", "bonmotdepasse")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    // --- POST /users/me/email/resend ---

    @Test
    public void testResendWithoutAnyAddressIsRefused() throws Exception {

        User user = stravaUser();

        mockMvc.perform(post("/users/me/email/resend")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(authMailService);

    }

    @Test
    public void testResendUsesThePendingAddressFirst() throws Exception {

        User user = fullUser();
        when(userAuthTokenService.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of("nouvelle@example.com"));

        mockMvc.perform(post("/users/me/email/resend")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(authMailService).sendEmailVerification("nouvelle@example.com", "clear-token");

    }

    // --- POST /users/me/unlink/{provider} ---

    @Test
    public void testUnlinkOfAnUnsupportedProviderIsRefused() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/unlink/strava")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).save(any(User.class));

    }

    /**
     * Deliaison refusee : ce serait le seul moyen de connexion du compte. Strava ne compte pas
     * comme identite de repli.
     */
    @Test
    public void testUnlinkOfTheLastIdentityIsRefused() throws Exception {

        User user = stravaUser();
        user.setGoogleId("google-sub");

        mockMvc.perform(post("/users/me/unlink/google")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        assertEquals("google-sub", user.getGoogleId());
        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testUnlinkIsAllowedWhenAPasswordLoginRemains() throws Exception {

        User user = fullUser();
        user.setGoogleId("google-sub");

        mockMvc.perform(post("/users/me/unlink/google")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        assertNull(user.getGoogleId());
        verify(userService).save(user);

    }

    @Test
    public void testUnlinkIsAllowedWhenTheOtherIdentityRemains() throws Exception {

        User user = stravaUser();
        user.setGoogleId("google-sub");
        user.setFacebookId("facebook-id");

        mockMvc.perform(post("/users/me/unlink/facebook")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        assertNull(user.getFacebookId());
        assertEquals("google-sub", user.getGoogleId());

    }

    // --- POST /users/me/delete ---

    @Test
    public void testDeleteRequiresAConnectedUser() throws Exception {

        mockMvc.perform(post("/users/me/delete"))
                .andExpect(view().name("redirect:/"));

        verify(userService, never()).delete(anyString());

    }

    @Test
    public void testDeleteRemovesTheAccountAndLogsOut() throws Exception {

        User user = fullUser();

        mockMvc.perform(post("/users/me/delete")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/logout"));

        verify(userService).delete("user-1");

    }

}
