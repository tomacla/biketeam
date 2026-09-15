package info.tomacla.biketeam.service.auth;

import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenRepository;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserAuthTokenServiceTest {

    private UserAuthTokenRepository repository;
    private UserAuthTokenService service;

    @BeforeEach
    public void setUp() throws Exception {
        repository = mock(UserAuthTokenRepository.class);
        service = new UserAuthTokenService();
        Field f = UserAuthTokenService.class.getDeclaredField("userAuthTokenRepository");
        f.setAccessible(true);
        f.set(service, repository);
        set("emailVerificationValidityHours", 24L);
        set("passwordResetValidityHours", 1L);

        when(repository.findByUserIdAndTypeAndConsumedAtIsNull(any(), any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void set(String field, Object value) throws Exception {
        Field f = UserAuthTokenService.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(service, value);
    }

    private UserAuthToken token(UserAuthTokenType type, String clear, Instant expiresAt) {
        UserAuthToken token = new UserAuthToken();
        token.setId("t1");
        token.setUserId("user-1");
        token.setType(type);
        token.setTokenHash(UserAuthTokenService.sha256Hex(clear));
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(Instant.now());
        return token;
    }

    @Test
    public void testCreateNeverPersistsClearToken() {

        String clearToken = service.create("user-1", UserAuthTokenType.EMAIL_VERIFICATION, "user@example.com", Duration.ofHours(1));

        ArgumentCaptor<UserAuthToken> captor = ArgumentCaptor.forClass(UserAuthToken.class);
        verify(repository).save(captor.capture());

        UserAuthToken saved = captor.getValue();
        assertNotEquals(clearToken, saved.getTokenHash());
        assertEquals(UserAuthTokenService.sha256Hex(clearToken), saved.getTokenHash());
        assertFalse(saved.getTokenHash().contains(clearToken));

    }

    @Test
    public void testCreateTokenLengthIsAtLeast43Characters() {

        // 32 octets aleatoires encodes en Base64 URL sans padding : 43 caracteres minimum
        String clearToken = service.create("user-1", UserAuthTokenType.PASSWORD_RESET, null, Duration.ofHours(1));
        assertTrue(clearToken.length() >= 43, "token too short: " + clearToken.length());

    }

    @Test
    public void testCreateInvalidatesActiveTokensOfSameType() {

        UserAuthToken existing = new UserAuthToken();
        existing.setId("t1");
        existing.setUserId("user-1");
        existing.setType(UserAuthTokenType.EMAIL_VERIFICATION);
        when(repository.findByUserIdAndTypeAndConsumedAtIsNull("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(List.of(existing));

        service.create("user-1", UserAuthTokenType.EMAIL_VERIFICATION, "user@example.com", Duration.ofHours(1));

        assertNotNull(existing.getConsumedAt());
        verify(repository, atLeastOnce()).save(existing);

    }

    @Test
    public void testConsumeUnknownTokenReturnsEmpty() {

        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        Optional<UserAuthToken> result = service.consume("does-not-exist", UserAuthTokenType.EMAIL_VERIFICATION);

        assertTrue(result.isEmpty());

    }

    @Test
    public void testConsumeExpiredTokenReturnsEmpty() {

        String clear = "clear-token-value";
        UserAuthToken token = new UserAuthToken();
        token.setId("t1");
        token.setType(UserAuthTokenType.PASSWORD_RESET);
        token.setTokenHash(UserAuthTokenService.sha256Hex(clear));
        token.setExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));

        when(repository.findByTokenHash(UserAuthTokenService.sha256Hex(clear))).thenReturn(Optional.of(token));

        Optional<UserAuthToken> result = service.consume(clear, UserAuthTokenType.PASSWORD_RESET);

        assertTrue(result.isEmpty());

    }

    @Test
    public void testConsumeAlreadyConsumedTokenReturnsEmpty() {

        String clear = "clear-token-value";
        UserAuthToken token = new UserAuthToken();
        token.setId("t1");
        token.setType(UserAuthTokenType.PASSWORD_RESET);
        token.setTokenHash(UserAuthTokenService.sha256Hex(clear));
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        token.setConsumedAt(Instant.now().minus(Duration.ofMinutes(1)));

        when(repository.findByTokenHash(UserAuthTokenService.sha256Hex(clear))).thenReturn(Optional.of(token));

        Optional<UserAuthToken> result = service.consume(clear, UserAuthTokenType.PASSWORD_RESET);

        assertTrue(result.isEmpty());

    }

    @Test
    public void testConsumeWrongTypeReturnsEmpty() {

        String clear = "clear-token-value";
        UserAuthToken token = new UserAuthToken();
        token.setId("t1");
        token.setType(UserAuthTokenType.EMAIL_VERIFICATION);
        token.setTokenHash(UserAuthTokenService.sha256Hex(clear));
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));

        when(repository.findByTokenHash(UserAuthTokenService.sha256Hex(clear))).thenReturn(Optional.of(token));

        Optional<UserAuthToken> result = service.consume(clear, UserAuthTokenType.PASSWORD_RESET);

        assertTrue(result.isEmpty());

    }

    @Test
    public void testConsumeValidTokenMarksConsumedAndReturnsIt() {

        String clear = "clear-token-value";
        UserAuthToken token = new UserAuthToken();
        token.setId("t1");
        token.setUserId("user-1");
        token.setType(UserAuthTokenType.PASSWORD_RESET);
        token.setTokenHash(UserAuthTokenService.sha256Hex(clear));
        token.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));

        when(repository.findByTokenHash(UserAuthTokenService.sha256Hex(clear))).thenReturn(Optional.of(token));

        Optional<UserAuthToken> result = service.consume(clear, UserAuthTokenType.PASSWORD_RESET);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getConsumedAt());
        verify(repository).save(token);

    }

    // --- durees de validite ---

    @Test
    public void testValidityDurationsComeFromConfiguration() throws Exception {

        assertEquals(Duration.ofHours(24), service.getEmailVerificationValidity());
        assertEquals(Duration.ofHours(1), service.getPasswordResetValidity());

        set("passwordResetValidityHours", 3L);
        assertEquals(Duration.ofHours(3), service.getPasswordResetValidity());

    }

    @Test
    public void testCreateAppliesTheRequestedTtlAndNormalizesTheTargetEmail() {

        service.create("user-1", UserAuthTokenType.EMAIL_VERIFICATION, "  Jean@Example.COM ", Duration.ofHours(2));

        ArgumentCaptor<UserAuthToken> captor = ArgumentCaptor.forClass(UserAuthToken.class);
        verify(repository).save(captor.capture());

        UserAuthToken saved = captor.getValue();
        assertEquals("jean@example.com", saved.getTargetEmail());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now().plus(Duration.ofMinutes(119))));
        assertTrue(saved.getExpiresAt().isBefore(Instant.now().plus(Duration.ofMinutes(121))));

    }

    @Test
    public void testCreateWithoutTargetEmailStoresNull() {

        service.create("user-1", UserAuthTokenType.PASSWORD_RESET, null, Duration.ofHours(1));

        ArgumentCaptor<UserAuthToken> captor = ArgumentCaptor.forClass(UserAuthToken.class);
        verify(repository).save(captor.capture());

        assertNull(captor.getValue().getTargetEmail());

    }

    @Test
    public void testTwoCreatedTokensAreNeverIdentical() {

        String first = service.create("user-1", UserAuthTokenType.PASSWORD_RESET, null, Duration.ofHours(1));
        String second = service.create("user-1", UserAuthTokenType.PASSWORD_RESET, null, Duration.ofHours(1));

        assertNotEquals(first, second);

    }

    // --- isUsable ---

    @Test
    public void testUsableTokenIsNeverConsumed() {

        String clear = "clear-token-value";
        when(repository.findByTokenHash(UserAuthTokenService.sha256Hex(clear)))
                .thenReturn(Optional.of(token(UserAuthTokenType.PASSWORD_RESET, clear,
                        Instant.now().plus(Duration.ofHours(1)))));

        assertTrue(service.isUsable(clear, UserAuthTokenType.PASSWORD_RESET));
        verify(repository, never()).save(any());

    }

    @Test
    public void testBlankTokenIsNotUsable() {

        assertFalse(service.isUsable(null, UserAuthTokenType.PASSWORD_RESET));
        assertFalse(service.isUsable("   ", UserAuthTokenType.PASSWORD_RESET));
        verify(repository, never()).findByTokenHash(any());

    }

    @Test
    public void testUnknownExpiredConsumedOrWrongTypeTokenIsNotUsable() {

        String clear = "clear-token-value";
        String hash = UserAuthTokenService.sha256Hex(clear);

        when(repository.findByTokenHash(hash)).thenReturn(Optional.empty());
        assertFalse(service.isUsable(clear, UserAuthTokenType.PASSWORD_RESET));

        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(
                token(UserAuthTokenType.PASSWORD_RESET, clear, Instant.now().minus(Duration.ofMinutes(1)))));
        assertFalse(service.isUsable(clear, UserAuthTokenType.PASSWORD_RESET));

        UserAuthToken consumed = token(UserAuthTokenType.PASSWORD_RESET, clear, Instant.now().plus(Duration.ofHours(1)));
        consumed.setConsumedAt(Instant.now());
        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(consumed));
        assertFalse(service.isUsable(clear, UserAuthTokenType.PASSWORD_RESET));

        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(
                token(UserAuthTokenType.EMAIL_VERIFICATION, clear, Instant.now().plus(Duration.ofHours(1)))));
        assertFalse(service.isUsable(clear, UserAuthTokenType.PASSWORD_RESET));

    }

    // --- invalidateAll ---

    @Test
    public void testInvalidateAllMarksEveryActiveTokenConsumed() {

        UserAuthToken first = token(UserAuthTokenType.PASSWORD_RESET, "a", Instant.now().plus(Duration.ofHours(1)));
        UserAuthToken second = token(UserAuthTokenType.PASSWORD_RESET, "b", Instant.now().plus(Duration.ofHours(1)));
        when(repository.findByUserIdAndTypeAndConsumedAtIsNull("user-1", UserAuthTokenType.PASSWORD_RESET))
                .thenReturn(List.of(first, second));

        service.invalidateAll("user-1", UserAuthTokenType.PASSWORD_RESET);

        assertNotNull(first.getConsumedAt());
        assertNotNull(second.getConsumedAt());

    }

    // --- getPendingTargetEmail ---

    @Test
    public void testPendingTargetEmailIsTheMostRecentLiveToken() {

        UserAuthToken older = token(UserAuthTokenType.EMAIL_VERIFICATION, "a", Instant.now().plus(Duration.ofHours(1)));
        older.setTargetEmail("ancienne@example.com");
        older.setCreatedAt(Instant.now().minus(Duration.ofHours(2)));

        UserAuthToken newer = token(UserAuthTokenType.EMAIL_VERIFICATION, "b", Instant.now().plus(Duration.ofHours(1)));
        newer.setTargetEmail("nouvelle@example.com");
        newer.setCreatedAt(Instant.now());

        when(repository.findByUserIdAndTypeAndConsumedAtIsNull("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(List.of(older, newer));

        assertEquals(Optional.of("nouvelle@example.com"),
                service.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION));

    }

    @Test
    public void testExpiredOrTargetlessTokensCarryNoPendingAddress() {

        UserAuthToken expired = token(UserAuthTokenType.EMAIL_VERIFICATION, "a", Instant.now().minus(Duration.ofMinutes(1)));
        expired.setTargetEmail("expiree@example.com");

        UserAuthToken targetless = token(UserAuthTokenType.EMAIL_VERIFICATION, "b", Instant.now().plus(Duration.ofHours(1)));

        when(repository.findByUserIdAndTypeAndConsumedAtIsNull("user-1", UserAuthTokenType.EMAIL_VERIFICATION))
                .thenReturn(List.of(expired, targetless));

        assertTrue(service.getPendingTargetEmail("user-1", UserAuthTokenType.EMAIL_VERIFICATION).isEmpty());

    }

    // --- isThrottled ---

    @Test
    public void testThrottlingTriggersOnceTheQuotaIsReached() {

        when(repository.countByUserIdAndTypeAndCreatedAtAfter(eq("user-1"), eq(UserAuthTokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(4L);
        assertFalse(service.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5));

        when(repository.countByUserIdAndTypeAndCreatedAtAfter(eq("user-1"), eq(UserAuthTokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(5L);
        assertTrue(service.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5));

    }

    // --- purgeExpired ---

    @Test
    public void testPurgeExpiredDeletesOutdatedTokens() {

        service.purgeExpired();

        verify(repository).deleteByExpiresAtBefore(any(Instant.class));

    }

    @Test
    public void testPurgeExpiredNeverPropagatesAFailure() {

        doThrow(new IllegalStateException("db down")).when(repository).deleteByExpiresAtBefore(any());

        assertDoesNotThrow(() -> service.purgeExpired());

    }

}
