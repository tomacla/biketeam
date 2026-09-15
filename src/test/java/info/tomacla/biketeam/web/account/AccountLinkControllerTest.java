package info.tomacla.biketeam.web.account;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.completion.AccountCompletionService;
import info.tomacla.biketeam.security.oauth2.link.AccountLinkService;
import info.tomacla.biketeam.security.oauth2.link.OAuth2LinkIntentStore;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Liaison d'un compte externe et resolution des conflits d'identite.
 * <p>
 * Invariants : seuls Google et Facebook sont liables (Strava est une connexion de transition),
 * l'intention n'est posee que par cette route, et la fusion confirmee conserve TOUJOURS le compte
 * de la session courante comme cible.
 */
public class AccountLinkControllerTest {

    private UserService userService;
    private OAuth2LinkIntentStore linkIntentStore;
    private AccountLinkService accountLinkService;
    private SecurityContextService securityContextService;
    private AccountCompletionService accountCompletionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        linkIntentStore = mock(OAuth2LinkIntentStore.class);
        accountLinkService = mock(AccountLinkService.class);
        securityContextService = mock(SecurityContextService.class);
        accountCompletionService = mock(AccountCompletionService.class);

        AccountLinkController controller = new AccountLinkController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "linkIntentStore", linkIntentStore);
        ReflectionTestUtils.setField(controller, "accountLinkService", accountLinkService);
        ReflectionTestUtils.setField(controller, "securityContextService", securityContextService);
        ReflectionTestUtils.setField(controller, "accountCompletionService", accountCompletionService);

        when(accountLinkService.getPendingMergeUserId(any())).thenReturn(Optional.empty());
        when(accountLinkService.getPendingMergeProvider(any())).thenReturn(Optional.empty());
        when(accountLinkService.getPendingMergeSubject(any())).thenReturn(Optional.empty());
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc = ControllerTestSupport.mockMvc(controller);

    }

    private User user(String id) {
        User user = new User();
        user.setId(id);
        when(userService.get(id)).thenReturn(Optional.of(user));
        return user;
    }

    private void pendingMerge(String otherUserId, String provider, String subject) {
        when(accountLinkService.getPendingMergeUserId(any(HttpSession.class)))
                .thenReturn(Optional.of(otherUserId));
        when(accountLinkService.getPendingMergeProvider(any(HttpSession.class)))
                .thenReturn(Optional.ofNullable(provider));
        when(accountLinkService.getPendingMergeSubject(any(HttpSession.class)))
                .thenReturn(Optional.ofNullable(subject));
    }

    // --- GET /account/link/{registrationId} ---

    @Test
    public void testLinkRequiresAConnectedUser() throws Exception {

        mockMvc.perform(get("/account/link/google"))
                .andExpect(view().name("redirect:/"));

        verifyNoInteractions(linkIntentStore);

    }

    @Test
    public void testLinkStoresTheIntentAndStartsTheAuthorization() throws Exception {

        User user = user("user-1");

        mockMvc.perform(get("/account/link/google").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/oauth2/authorization/google"));

        verify(linkIntentStore).storeIntent("google");

    }

    @Test
    public void testFacebookIsLinkable() throws Exception {

        User user = user("user-1");

        mockMvc.perform(get("/account/link/facebook").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/oauth2/authorization/facebook"));

        verify(linkIntentStore).storeIntent("facebook");

    }

    /**
     * Strava n'est volontairement pas liable : aucune intention ne doit etre posee.
     */
    @Test
    public void testStravaIsNotLinkable() throws Exception {

        User user = user("user-1");

        mockMvc.perform(get("/account/link/strava").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(linkIntentStore);

    }

    @Test
    public void testUnknownProviderIsRefused() throws Exception {

        User user = user("user-1");

        mockMvc.perform(get("/account/link/evil").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(linkIntentStore);

    }

    // --- GET /account/link-conflict ---

    @Test
    public void testConflictPageRequiresAConnectedUser() throws Exception {

        mockMvc.perform(get("/account/link-conflict"))
                .andExpect(view().name("redirect:/"));

    }

    @Test
    public void testConflictPageRequiresAPendingMerge() throws Exception {

        User user = user("user-1");

        mockMvc.perform(get("/account/link-conflict").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

    }

    @Test
    public void testConflictPageShowsBothAccounts() throws Exception {

        User user = user("user-1");
        User other = user("user-2");
        pendingMerge("user-2", "google", "google-sub");

        mockMvc.perform(get("/account/link-conflict").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("account_merge"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("otherUser", other))
                .andExpect(model().attribute("provider", "google"));

    }

    // --- POST /account/link-conflict ---

    @Test
    public void testMergeRequiresAConnectedUser() throws Exception {

        mockMvc.perform(post("/account/link-conflict"))
                .andExpect(view().name("redirect:/"));

        verify(userService, never()).merge(anyString(), anyString());

    }

    @Test
    public void testMergeRequiresAPendingMerge() throws Exception {

        User user = user("user-1");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verify(userService, never()).merge(anyString(), anyString());

    }

    /**
     * Le compte en attente est le compte courant : rien a fusionner, l'attente est simplement
     * effacee.
     */
    @Test
    public void testMergeOfTheSameAccountIsANoOp() throws Exception {

        User user = user("user-1");
        pendingMerge("user-1", "google", "google-sub");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(user)))
                .andExpect(view().name("redirect:/users/me"));

        verify(userService, never()).merge(anyString(), anyString());
        verify(accountLinkService).clearPendingMerge(any());

    }

    /**
     * Le sens de la fusion est impose : la cible est TOUJOURS le compte de la session courante.
     * L'identite portee par le compte source est reprise sur la cible.
     */
    @Test
    public void testMergeKeepsTheCurrentAccountAndTakesOverTheIdentity() throws Exception {

        User target = user("user-1");
        User source = user("user-2");
        source.setGoogleId("google-sub-1");
        pendingMerge("user-2", "google", "google-sub-1");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(target)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("infos"));

        verify(userService).merge("user-2", "user-1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertEquals("google-sub-1", captor.getValue().getGoogleId());
        assertEquals("user-1", captor.getValue().getId());

        verify(securityContextService).refreshCurrentUser(eq(target), any(), any());
        verify(accountCompletionService).invalidate(any());
        verify(accountLinkService).clearPendingMerge(any());

    }

    /**
     * Conflit declenche par l'ADRESSE : l'autre compte ne porte aucune identite du fournisseur.
     * Sans reprise du sujet prouve lors de la tentative, la fusion aboutirait sans realiser la
     * liaison demandee.
     */
    @Test
    public void testMergeTakesOverTheProvenSubjectWhenTheSourceCarriesNoIdentity() throws Exception {

        User target = user("user-1");
        user("user-2");
        pendingMerge("user-2", "google", "proven-google-sub");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(target)))
                .andExpect(view().name("redirect:/users/me"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertEquals("proven-google-sub", captor.getValue().getGoogleId());

    }

    @Test
    public void testMergeTakesOverTheProvenFacebookSubject() throws Exception {

        User target = user("user-1");
        user("user-2");
        pendingMerge("user-2", "facebook", "proven-facebook-id");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(target)))
                .andExpect(view().name("redirect:/users/me"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        assertEquals("proven-facebook-id", captor.getValue().getFacebookId());

    }

    /**
     * Une identite deja portee par la cible n'est jamais ecrasee par celle du compte source.
     */
    @Test
    public void testExistingIdentityOfTheTargetIsNeverOverwritten() throws Exception {

        User target = user("user-1");
        target.setGoogleId("google-of-target");
        User source = user("user-2");
        source.setGoogleId("google-of-source");
        pendingMerge("user-2", "google", "google-of-source");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(target)))
                .andExpect(view().name("redirect:/users/me"));

        verify(userService, never()).save(any(User.class));
        assertEquals("google-of-target", target.getGoogleId());

    }

    @Test
    public void testMergeFailureIsReportedAndChangesNothing() throws Exception {

        User target = user("user-1");
        user("user-2");
        pendingMerge("user-2", "google", "google-sub");
        doThrow(new IllegalStateException("boom")).when(userService).merge("user-2", "user-1");

        mockMvc.perform(post("/account/link-conflict").principal(ControllerTestSupport.authentication(target)))
                .andExpect(view().name("redirect:/users/me"))
                .andExpect(flash().attributeExists("errors"));

        verifyNoInteractions(securityContextService);
        verify(accountLinkService, never()).clearPendingMerge(any());

    }

}
