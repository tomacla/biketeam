package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import info.tomacla.biketeam.service.merge.UserMergeService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Rattachement d'un compte incomplet a un compte existant.
 * <p>
 * Invariants critiques :
 * <ul>
 * <li>DEUX preuves sont exigees : le lien recu par email et la session du compte demandeur ;</li>
 * <li>le token n'est jamais consomme tant que la session n'est pas la bonne, sans quoi le lien
 *     serait brule par la premiere ouverture depuis le mauvais navigateur ;</li>
 * <li>le compte conserve est celui epingle par le token, jamais celui de la session : c'est
 *     l'inverse du sens impose par AccountLinkController.</li>
 * </ul>
 */
public class AccountMergeControllerTest {

    private UserService userService;
    private UserAuthTokenService userAuthTokenService;
    private UserMergeService userMergeService;
    private SecurityContextService securityContextService;
    private AccountCompletionService accountCompletionService;

    private MockMvc mockMvc;

    private final Map<String, User> accounts = new HashMap<>();

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        userAuthTokenService = mock(UserAuthTokenService.class);
        userMergeService = mock(UserMergeService.class);
        securityContextService = mock(SecurityContextService.class);
        accountCompletionService = mock(AccountCompletionService.class);

        AccountMergeController controller = new AccountMergeController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(controller, "userMergeService", userMergeService);
        ReflectionTestUtils.setField(controller, "securityContextService", securityContextService);
        ReflectionTestUtils.setField(controller, "accountCompletionService", accountCompletionService);

        when(userAuthTokenService.peek(anyString(), any())).thenReturn(Optional.empty());
        when(userService.merge(anyString(), anyString())).thenAnswer(invocation ->
                accounts.get(invocation.getArgument(1, String.class)));

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    private User user(String id) {
        User user = new User();
        user.setId(id);
        when(userService.get(id)).thenReturn(Optional.of(user));
        accounts.put(id, user);
        return user;
    }

    /**
     * Token valide pointant de {@code sourceId} (demandeur) vers {@code targetId} (conserve).
     */
    private UserAuthToken token(String sourceId, String targetId) {
        UserAuthToken token = new UserAuthToken();
        token.setUserId(sourceId);
        token.setRelatedUserId(targetId);
        token.setType(UserAuthTokenType.ACCOUNT_MERGE);
        token.setTargetEmail("owner@example.com");
        when(userAuthTokenService.peek("code", UserAuthTokenType.ACCOUNT_MERGE))
                .thenReturn(Optional.of(token));
        when(userAuthTokenService.consume("code", UserAuthTokenType.ACCOUNT_MERGE))
                .thenReturn(Optional.of(token));
        return token;
    }

    /**
     * Session telle que l'ecran de confirmation vient de la laisser.
     */
    private MockHttpSession pendingMerge(String sourceId, String targetId, Instant createdAt) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AccountMergeController.PENDING_MERGE_SOURCE, sourceId);
        session.setAttribute(AccountMergeController.PENDING_MERGE_TARGET, targetId);
        session.setAttribute(AccountMergeController.PENDING_MERGE_CREATED_AT, createdAt.toString());
        return session;
    }

    // --- GET /account/merge ---

    @Test
    public void testInvalidTokenIsRejected() throws Exception {

        User source = user("strava-1");

        mockMvc.perform(get("/account/merge").param("code", "unknown")
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(anyString(), any());

    }

    /**
     * Ouverture depuis un navigateur non connecte : le lien doit rester utilisable.
     */
    @Test
    public void testLinkOpenedWithoutASessionIsNotConsumed() throws Exception {

        user("strava-1");
        user("email-1");
        token("strava-1", "email-1");

        mockMvc.perform(get("/account/merge").param("code", "code"))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(anyString(), any());

    }

    /**
     * Ouverture depuis la session d'un tiers : ni consommation, ni fusion.
     */
    @Test
    public void testLinkOpenedFromAnotherAccountIsNotConsumed() throws Exception {

        user("strava-1");
        user("email-1");
        User intruder = user("intruder-1");
        token("strava-1", "email-1");

        mockMvc.perform(get("/account/merge").param("code", "code")
                        .principal(ControllerTestSupport.authentication(intruder)))
                .andExpect(view().name("redirect:/login"))
                .andExpect(flash().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(anyString(), any());

    }

    @Test
    public void testConfirmationPageShowsTheKeptAccountFirstAndArmsTheSession() throws Exception {

        User source = user("strava-1");
        User target = user("email-1");
        token("strava-1", "email-1");
        when(userMergeService.wouldLosePrivateTeam(source, target)).thenReturn(true);

        HttpSession session = mockMvc.perform(get("/account/merge").param("code", "code")
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("account_merge"))
                // "user" est le compte CONSERVE : ici le compte email, pas celui de la session
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .model().attribute("user", target))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .model().attribute("otherUser", source))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .model().attribute("privateTeamLost", true))
                .andReturn().getRequest().getSession(false);

        verify(userAuthTokenService).consume("code", UserAuthTokenType.ACCOUNT_MERGE);

        org.junit.jupiter.api.Assertions.assertEquals("strava-1",
                session.getAttribute(AccountMergeController.PENDING_MERGE_SOURCE));
        org.junit.jupiter.api.Assertions.assertEquals("email-1",
                session.getAttribute(AccountMergeController.PENDING_MERGE_TARGET));

    }

    @Test
    public void testVanishedTargetAccountIsRejected() throws Exception {

        User source = user("strava-1");
        token("strava-1", "email-1");
        when(userService.get("email-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/account/merge").param("code", "code")
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(anyString(), any());

    }

    /**
     * Un compte deja supprime ne peut pas etre la cible : la purge asynchrone emporterait les
     * donnees qu'on vient de lui transferer.
     */
    @Test
    public void testTargetAccountPendingDeletionIsRejected() throws Exception {

        User source = user("strava-1");
        User target = user("email-1");
        target.setDeletion(true);
        token("strava-1", "email-1");

        mockMvc.perform(get("/account/merge").param("code", "code")
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userAuthTokenService, never()).consume(anyString(), any());

    }

    // --- POST /account/merge ---

    @Test
    public void testConfirmationWithoutAPendingMergeIsRejected() throws Exception {

        User source = user("strava-1");

        mockMvc.perform(post("/account/merge")
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).merge(anyString(), anyString());

    }

    @Test
    public void testExpiredConfirmationIsRejected() throws Exception {

        User source = user("strava-1");
        user("email-1");

        mockMvc.perform(post("/account/merge")
                        .session(pendingMerge("strava-1", "email-1", Instant.now().minus(30, ChronoUnit.MINUTES)))
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).merge(anyString(), anyString());

    }

    /**
     * La session a pu changer de compte entre l'affichage de l'ecran et la confirmation.
     */
    @Test
    public void testConfirmationFromAnotherAccountIsRejected() throws Exception {

        user("strava-1");
        user("email-1");
        User intruder = user("intruder-1");

        MockHttpSession session = pendingMerge("strava-1", "email-1", Instant.now());

        mockMvc.perform(post("/account/merge").session(session)
                        .principal(ControllerTestSupport.authentication(intruder)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).merge(anyString(), anyString());
        assertNull(session.getAttribute(AccountMergeController.PENDING_MERGE_SOURCE));

    }

    /**
     * Cas nominal : le compte email est conserve, et la session bascule dessus -- son propre
     * compte vient d'etre supprime.
     */
    @Test
    public void testConfirmedMergeKeepsTheEmailAccountAndRebindsTheSession() throws Exception {

        User source = user("strava-1");
        User target = user("email-1");

        MockHttpSession session = pendingMerge("strava-1", "email-1", Instant.now());

        mockMvc.perform(post("/account/merge").session(session)
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(userService).merge("strava-1", "email-1");
        verify(securityContextService).refreshCurrentUser(eq(target), any(), any());
        verify(accountCompletionService).invalidate(any());

        assertNull(session.getAttribute(AccountMergeController.PENDING_MERGE_SOURCE));

    }

    @Test
    public void testMergeFailureIsReportedAndRebindsNothing() throws Exception {

        User source = user("strava-1");
        user("email-1");

        org.mockito.Mockito.doThrow(new IllegalStateException("boom"))
                .when(userService).merge("strava-1", "email-1");

        MockHttpSession session = pendingMerge("strava-1", "email-1", Instant.now());

        mockMvc.perform(post("/account/merge").session(session)
                        .principal(ControllerTestSupport.authentication(source)))
                .andExpect(view().name("redirect:/account/complete"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(securityContextService);
        assertNull(session.getAttribute(AccountMergeController.PENDING_MERGE_SOURCE));

    }

}
