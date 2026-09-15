package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.data.client.Origin;
import info.tomacla.biketeam.domain.user.UserPasskey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests des gardes qui entourent la verification cryptographique.
 * <p>
 * La verification de signature elle-meme n'est pas rejouee ici : elle appartient a webauthn4j et
 * exigerait un authentificateur virtuel (module webauthn4j-test). Ce qui est teste est ce que
 * l'application ajoute autour : consommation du challenge, resolution du compte, coherence du
 * userHandle, uniformite des messages d'erreur.
 */
public class PasskeyAuthenticationServiceTest {

    private PasskeyChallengeStore challengeStore;
    private PasskeyService passkeyService;
    private PasskeyProperties passkeyProperties;
    private PasskeyAuthenticationService service;

    @BeforeEach
    public void setUp() {

        challengeStore = mock(PasskeyChallengeStore.class);
        passkeyService = mock(PasskeyService.class);
        passkeyProperties = mock(PasskeyProperties.class);

        when(passkeyProperties.getRpId()).thenReturn("biketeam.info");
        when(passkeyProperties.getOrigins()).thenReturn(Set.of(Origin.create("https://biketeam.info")));
        when(passkeyProperties.getChallengeValidity()).thenReturn(Duration.ofMinutes(5));

        service = new PasskeyAuthenticationService();
        ReflectionTestUtils.setField(service, "challengeStore", challengeStore);
        ReflectionTestUtils.setField(service, "passkeyService", passkeyService);
        ReflectionTestUtils.setField(service, "passkeyProperties", passkeyProperties);

    }

    private PasskeyAuthenticationBody body(String credentialId, String userHandle) {
        return new PasskeyAuthenticationBody(credentialId, userHandle, "AAAA", "AAAA", "AAAA", null);
    }

    /**
     * allowCredentials doit etre PRESENT et VIDE : absent, le navigateur ne bascule pas en mode
     * credential decouvrable et la connexion sans identifiant ne fonctionne plus.
     */
    @Test
    public void testOptionsRequestDiscoverableCredentials() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.newAuthenticationChallenge(request))
                .thenReturn(new PasskeyChallenge(new byte[]{1, 2, 3}, java.time.Instant.MAX, null));

        Map<String, Object> options = service.createOptions(request);

        assertEquals("biketeam.info", options.get("rpId"));
        assertEquals(List.of(), options.get("allowCredentials"));
        assertEquals("preferred", options.get("userVerification"));
        assertEquals(Base64Url.encode(new byte[]{1, 2, 3}), options.get("challenge"));

    }

    @Test
    public void testMissingChallengeIsRejectedBeforeAnyLookup() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.consumeAuthenticationChallenge(request)).thenReturn(Optional.empty());

        assertThrows(PasskeyException.class, () -> service.verify(request, body("YQ", null)));

        verifyNoInteractions(passkeyService);

    }

    @Test
    public void testUnknownCredentialIsRejected() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.consumeAuthenticationChallenge(request))
                .thenReturn(Optional.of(new PasskeyChallenge(new byte[]{1}, java.time.Instant.MAX, null)));
        when(passkeyService.getByCredentialId(anyString())).thenReturn(Optional.empty());

        PasskeyException e = assertThrows(PasskeyException.class,
                () -> service.verify(request, body("YQ", null)));

        assertEquals(PasskeyAuthenticationService.MSG_FAILED, e.getMessage());

    }

    /**
     * Le message doit etre le MEME que pour un credential inconnu : sinon l'endpoint permet de
     * distinguer une passkey enregistree d'une passkey inventee.
     */
    @Test
    public void testMalformedPayloadYieldsSameMessage() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.consumeAuthenticationChallenge(request))
                .thenReturn(Optional.of(new PasskeyChallenge(new byte[]{1}, java.time.Instant.MAX, null)));

        PasskeyException e = assertThrows(PasskeyException.class, () ->
                service.verify(request, new PasskeyAuthenticationBody(null, null, null, null, null, null)));

        assertEquals(PasskeyAuthenticationService.MSG_FAILED, e.getMessage());
        verifyNoInteractions(passkeyService);

    }

    /**
     * Le userHandle renvoye par l'authentificateur doit designer le compte porteur du credential.
     * webauthn4j ne fait pas ce rapprochement : il ne connait pas le modele de l'application.
     */
    @Test
    public void testInconsistentUserHandleIsRejected() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.consumeAuthenticationChallenge(request))
                .thenReturn(Optional.of(new PasskeyChallenge(new byte[]{1}, java.time.Instant.MAX, null)));

        UserPasskey passkey = new UserPasskey();
        passkey.setId("pk-1");
        passkey.setUserId("user-1");
        when(passkeyService.getByCredentialId("YQ")).thenReturn(Optional.of(passkey));

        String otherHandle = Base64Url.encode("user-2".getBytes(StandardCharsets.UTF_8));

        PasskeyException e = assertThrows(PasskeyException.class,
                () -> service.verify(request, body("YQ", otherHandle)));

        assertEquals(PasskeyAuthenticationService.MSG_FAILED, e.getMessage());

    }

    /**
     * Une signature invalide ne doit jamais mettre a jour le compteur : ce serait un moyen de
     * faire avancer artificiellement le compteur d'une passkey legitime.
     */
    @Test
    public void testInvalidAssertionDoesNotRecordUsage() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        when(challengeStore.consumeAuthenticationChallenge(request))
                .thenReturn(Optional.of(new PasskeyChallenge(new byte[]{1}, java.time.Instant.MAX, null)));

        UserPasskey passkey = new UserPasskey();
        passkey.setId("pk-1");
        passkey.setUserId("user-1");
        passkey.setAttestedCredentialData(new byte[]{0});
        when(passkeyService.getByCredentialId("YQ")).thenReturn(Optional.of(passkey));
        when(passkeyService.toCredentialRecord(passkey)).thenThrow(new IllegalArgumentException("donnees illisibles"));

        assertThrows(RuntimeException.class, () -> service.verify(request, body("YQ", null)));

        verify(passkeyService, org.mockito.Mockito.never())
                .recordUsage(anyString(), org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.anyBoolean());

    }

}
