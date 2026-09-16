package info.tomacla.biketeam.web.auth;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Consommation d'un lien de verification d'adresse.
 * <p>
 * Aucune connexion automatique n'est realisee : la reception du mail prouve le controle de la
 * boite, pas l'intention de se connecter depuis ce navigateur. Lorsque le lien est ouvert par le
 * titulaire deja connecte, le principal DOIT etre rafraichi dans la meme requete, sinon il reste
 * bloque par AccountCompletionFilter.
 */
public class EmailVerificationControllerTest {

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private SecurityContextService securityContextService;
    private AccountCompletionService accountCompletionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        securityContextService = mock(SecurityContextService.class);
        accountCompletionService = mock(AccountCompletionService.class);

        EmailVerificationController controller = new EmailVerificationController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "securityContextService", securityContextService);
        ReflectionTestUtils.setField(controller, "accountCompletionService", accountCompletionService);

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
        token.setType(UserAuthTokenType.EMAIL_VERIFICATION);
        token.setTargetEmail(targetEmail);
        return token;
    }

    private void given(User user) {
        when(userService.get(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    public void testInvalidTokenRedirectsToLogin() throws Exception {

        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/verify-email").param("code", "dead-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).save(any(User.class));

    }

    /**
     * Visiteur non connecte : l'adresse est confirmee mais aucune session n'est ouverte.
     */
    @Test
    public void testAnonymousVisitorConfirmsAndIsSentToLogin() throws Exception {

        User user = user("user-1", "jean@example.com");
        given(user);
        when(userAuthTokenService.consume("live-code", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));

        mockMvc.perform(get("/verify-email").param("code", "live-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("infos"));

        assertTrue(user.isEmailVerified());
        verify(userService).save(user);
        verifyNoInteractions(securityContextService);

    }

    @Test
    public void testTokenOfAnUnknownUserIsRefused() throws Exception {

        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.of(token("gone", "jean@example.com")));
        when(userService.get("gone")).thenReturn(Optional.empty());

        mockMvc.perform(get("/verify-email").param("code", "live-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

    }

    /**
     * Ni adresse cible sur le token ni adresse sur le compte : rien a verifier.
     */
    @Test
    public void testTokenWithoutAnyAddressIsRefused() throws Exception {

        User user = user("user-1", null);
        given(user);
        when(userAuthTokenService.consume(any(), any())).thenReturn(Optional.of(token("user-1", null)));

        mockMvc.perform(get("/verify-email").param("code", "live-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).save(any(User.class));

    }

    /**
     * L'adresse a pu etre verifiee par un tiers entre l'emission du lien et le clic.
     */
    @Test
    public void testAddressTakenMeanwhileIsRefused() throws Exception {

        User user = user("user-1", "jean@example.com");
        given(user);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));
        when(userService.isEmailAvailable("jean@example.com", "user-1")).thenReturn(false);

        mockMvc.perform(get("/verify-email").param("code", "live-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).save(any(User.class));

    }

    @Test
    public void testConcurrentVerificationIsRefusedWithoutFailing() throws Exception {

        User user = user("user-1", "jean@example.com");
        given(user);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));
        when(userService.save(any(User.class))).thenThrow(new DataIntegrityViolationException("unique"));

        mockMvc.perform(get("/verify-email").param("code", "live-code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

    }

    /**
     * Titulaire deja connecte disposant deja d'un mot de passe : le principal est rafraichi et le
     * cache de completion invalide dans la meme requete.
     */
    @Test
    public void testConnectedOwnerIsRefreshedAndSentToProfile() throws Exception {

        User user = user("user-1", "jean@example.com");
        user.setPasswordHash("$2a$10$hash");
        given(user);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));

        mockMvc.perform(get("/verify-email").param("code", "live-code")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(securityContextService).refreshCurrentUser(eq(user), any(), any());
        verify(accountCompletionService).invalidate(any());

    }

    /**
     * Compte Strava qui vient de confirmer son adresse : il lui reste a choisir un mot de passe.
     */
    @Test
    public void testConnectedOwnerWithoutPasswordIsSentToCompletion() throws Exception {

        User user = user("user-1", "jean@example.com");
        given(user);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));

        mockMvc.perform(get("/verify-email").param("code", "live-code")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("infos"));

        verify(securityContextService).refreshCurrentUser(eq(user), any(), any());

    }

    /**
     * Lien d'un autre compte ouvert depuis une session : aucune connexion automatique, et surtout
     * aucun rafraichissement du principal courant.
     */
    @Test
    public void testLinkOfAnotherAccountNeverTouchesTheCurrentSession() throws Exception {

        User connected = user("user-1", "jean@example.com");
        connected.setPasswordHash("$2a$10$hash");
        given(connected);

        User other = user("user-2", "paul@example.com");
        given(other);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-2", "paul@example.com")));

        mockMvc.perform(get("/verify-email").param("code", "live-code")
                        .principal(ControllerTestSupport.authentication(connected)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("infos"));

        assertTrue(other.isEmailVerified());
        verifyNoInteractions(securityContextService);
        verifyNoInteractions(accountCompletionService);

    }

    /**
     * Lien perime ouvert par un utilisateur connecte : il revient sur son profil, pas sur la page
     * de connexion (il n'a aucune raison d'etre deconnecte).
     */
    @Test
    public void testExpiredLinkOfTheConnectedOwnerReturnsToProfile() throws Exception {

        User user = user("user-1", "jean@example.com");
        user.setPasswordHash("$2a$10$hash");
        given(user);
        when(userAuthTokenService.consume(any(), any()))
                .thenReturn(Optional.of(token("user-1", "jean@example.com")));
        when(userService.isEmailAvailable("jean@example.com", "user-1")).thenReturn(false);

        mockMvc.perform(get("/verify-email").param("code", "live-code")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

    }

}
