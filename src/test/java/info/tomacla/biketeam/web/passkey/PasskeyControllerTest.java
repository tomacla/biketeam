package info.tomacla.biketeam.web.passkey;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.passkey.PasskeyAuthenticationService;
import info.tomacla.biketeam.security.passkey.PasskeyException;
import info.tomacla.biketeam.security.passkey.PasskeyProperties;
import info.tomacla.biketeam.security.passkey.PasskeyRegistrationService;
import info.tomacla.biketeam.security.passkey.PasskeyService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PasskeyControllerTest {

    private PasskeyController controller;
    private MockMvc mockMvc;

    private UserService userService;
    private PasskeyService passkeyService;
    private PasskeyRegistrationService passkeyRegistrationService;
    private PasskeyAuthenticationService passkeyAuthenticationService;
    private RateLimitService rateLimitService;

    private User user;

    @BeforeEach
    public void setUp() {

        controller = new PasskeyController();

        userService = mock(UserService.class);
        passkeyService = mock(PasskeyService.class);
        passkeyRegistrationService = mock(PasskeyRegistrationService.class);
        passkeyAuthenticationService = mock(PasskeyAuthenticationService.class);
        rateLimitService = mock(RateLimitService.class);

        PasskeyProperties passkeyProperties = mock(PasskeyProperties.class);
        when(passkeyProperties.getLoginOptionsMaxPerHour()).thenReturn(60);

        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "passkeyService", passkeyService);
        ReflectionTestUtils.setField(controller, "passkeyRegistrationService", passkeyRegistrationService);
        ReflectionTestUtils.setField(controller, "passkeyAuthenticationService", passkeyAuthenticationService);
        ReflectionTestUtils.setField(controller, "rateLimitService", rateLimitService);
        ReflectionTestUtils.setField(controller, "passkeyProperties", passkeyProperties);

        mockMvc = ControllerTestSupport.mockMvc(controller);

        user = new User();
        user.setId("user-1");
        user.setVerifiedEmail("user@example.com");
        user.setAuthTokenSeed("seed");
        when(userService.get("user-1")).thenReturn(Optional.of(user));

        when(rateLimitService.clientKey(any(HttpServletRequest.class), anyString())).thenReturn("key");
        when(rateLimitService.tryAcquire(anyString(), anyInt())).thenReturn(true);

    }

    private Authentication authenticated() {
        return ControllerTestSupport.authentication(user);
    }

    @Test
    public void testLoginOptionsAreServedWithoutAuthentication() throws Exception {

        when(passkeyAuthenticationService.createOptions(any(HttpServletRequest.class)))
                .thenReturn(Map.of("challenge", "abc", "allowCredentials", List.of()));

        mockMvc.perform(post("/login/webauthn/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").value("abc"));

    }

    /**
     * La route est publique et cree une session : sans quota, elle serait un generateur gratuit
     * de sessions cote serveur.
     */
    @Test
    public void testLoginOptionsAreRateLimited() throws Exception {

        when(rateLimitService.tryAcquire(anyString(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/login/webauthn/options"))
                .andExpect(status().isTooManyRequests());

        verify(passkeyAuthenticationService, never()).createOptions(any(HttpServletRequest.class));

    }

    /**
     * Regle produit : une passkey ne s'enregistre que sur un compte deja connecte.
     */
    @Test
    public void testRegistrationOptionsRequireAuthentication() throws Exception {

        mockMvc.perform(post("/users/me/passkeys/options"))
                .andExpect(status().isUnauthorized());

        verify(passkeyRegistrationService, never())
                .createOptions(any(HttpServletRequest.class), any(User.class));

    }

    @Test
    public void testRegistrationOptionsAreServedToConnectedUser() throws Exception {

        when(passkeyRegistrationService.createOptions(any(HttpServletRequest.class), eq(user)))
                .thenReturn(Map.of("challenge", "abc"));

        mockMvc.perform(post("/users/me/passkeys/options").principal(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").value("abc"));

    }

    /**
     * Un echec fonctionnel se rend en JSON avec un 400 : le script client affiche le message,
     * et la supervision n'est pas polluee par les abandons d'utilisateur.
     */
    @Test
    public void testRegistrationFailureIsRenderedAsJson() throws Exception {

        when(passkeyRegistrationService.finish(any(HttpServletRequest.class), eq(user), any()))
                .thenThrow(new PasskeyException("Cette passkey est déjà enregistrée."));

        mockMvc.perform(post("/users/me/passkeys")
                        .principal(authenticated())
                        .contentType("application/json")
                        .content("{\"attestationObject\":\"AA\",\"clientDataJSON\":\"AA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cette passkey est déjà enregistrée."));

    }

    /**
     * La suppression ne doit jamais s'appuyer sur le seul identifiant d'URL : le service la
     * restreint au proprietaire, le controleur se contente de rapporter l'echec.
     */
    @Test
    public void testDeletingAnotherUsersPasskeyReportsNotFound() throws Exception {

        when(passkeyService.delete("pk-9", "user-1")).thenReturn(false);

        mockMvc.perform(post("/users/me/passkeys/pk-9/delete").principal(authenticated()))
                .andExpect(redirectedUrl("/users/me"))
                .andExpect(flash().attributeExists("errors"));

    }

    @Test
    public void testDeletingOwnPasskeySucceeds() throws Exception {

        when(passkeyService.delete("pk-1", "user-1")).thenReturn(true);

        mockMvc.perform(post("/users/me/passkeys/pk-1/delete").principal(authenticated()))
                .andExpect(redirectedUrl("/users/me"))
                .andExpect(flash().attributeExists("infos"));

    }

    @Test
    public void testDeleteWithoutAuthenticationRedirectsHome() throws Exception {

        mockMvc.perform(post("/users/me/passkeys/pk-1/delete"))
                .andExpect(redirectedUrl("/"));

        verify(passkeyService, never()).delete(anyString(), anyString());

    }

}
