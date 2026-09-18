package info.tomacla.biketeam.service.auth;

import info.tomacla.biketeam.service.auth.BotProtectionService.Verdict;
import org.altcha.altcha.v2.Altcha;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Protection anti-robot de l'inscription et du mot de passe oublie.
 * <p>
 * Les preuves de travail sont reellement resolues (avec un cout reduit) : le test verifie la
 * compatibilite de bout en bout avec le format du widget, pas seulement la logique d'aiguillage.
 */
public class BotProtectionServiceTest {

    private BotProtectionService service;

    /**
     * La bibliotheque ALTCHA controle l'expiration des defis avec l'horloge reelle : l'horloge
     * figee du service doit en partir.
     */
    private Instant start;

    @BeforeEach
    public void setUp() {
        start = Instant.now();
        service = new BotProtectionService();
        ReflectionTestUtils.setField(service, "minFillSeconds", 3);
        ReflectionTestUtils.setField(service, "altchaCost", 10);
        ReflectionTestUtils.setField(service, "altchaValidityMinutes", 15);
        setNow(start);
    }

    private void setNow(Instant now) {
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(now, ZoneOffset.UTC));
    }

    private String solve(String challengeJson) throws Exception {
        final JSONObject json = new JSONObject(challengeJson);
        final JSONObject params = json.getJSONObject("parameters");
        final Altcha.Challenge challenge = new Altcha.Challenge(new Altcha.ChallengeParameters(
                params.getString("algorithm"),
                params.getString("nonce"),
                params.getString("salt"),
                params.getInt("cost"),
                params.getInt("keyLength"),
                params.getString("keyPrefix"),
                null, null, null,
                params.getLong("expiresAt"),
                null), json.getString("signature"));
        final Altcha.Solution solution = Altcha.solveChallenge(challenge, Altcha.kdf(challenge.parameters().algorithm()));
        final JSONObject payload = new JSONObject()
                .put("challenge", json)
                .put("solution", new JSONObject()
                        .put("counter", solution.counter())
                        .put("derivedKey", solution.derivedKey()));
        return Base64.getEncoder().encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Formulaire affiche maintenant, envoye apres le delai minimal.
     */
    private String stampThenWait() {
        final String stamp = service.issueFormStamp();
        setNow(start.plusSeconds(10));
        return stamp;
    }

    @Test
    public void testHumanSubmissionIsAccepted() throws Exception {

        final String altcha = solve(service.createChallenge());
        final String stamp = stampThenWait();

        assertEquals(Verdict.HUMAN, service.check(altcha, "", stamp));
        assertEquals(Verdict.HUMAN, service.check(solve(service.createChallenge()), null, stamp),
                "le champ piege absent de la requete n'est pas un signal de robot");

    }

    @Test
    public void testFilledHoneypotIsDetectedFirst() throws Exception {

        final String altcha = solve(service.createChallenge());
        final String stamp = stampThenWait();

        assertEquals(Verdict.HONEYPOT, service.check(altcha, "https://spam.example", stamp));
        assertEquals(Verdict.HONEYPOT, service.check(null, "x", null));

    }

    @Test
    public void testSubmissionFasterThanTheMinimalDelayIsRefused() throws Exception {

        final String altcha = solve(service.createChallenge());
        final String stamp = service.issueFormStamp();

        setNow(start.plusSeconds(2));
        assertEquals(Verdict.TOO_FAST, service.check(altcha, "", stamp));

        // la preuve de travail n'a pas ete consommee par l'envoi refuse
        setNow(start.plusSeconds(3));
        assertEquals(Verdict.HUMAN, service.check(altcha, "", stamp));

    }

    @Test
    public void testMissingOrForgedStampIsRefused() throws Exception {

        final String altcha = solve(service.createChallenge());
        final String stamp = stampThenWait();
        final String forgedTimestamp = "1000" + stamp.substring(stamp.indexOf('.'));

        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", null));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", "garbage"));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", forgedTimestamp));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", stamp + "0"));

    }

    @Test
    public void testStampFromAnotherInstanceIsRefused() throws Exception {

        final String foreignStamp = new BotProtectionService().issueFormStamp();
        setNow(start.plusSeconds(10));

        assertEquals(Verdict.CHALLENGE_FAILED,
                service.check(solve(service.createChallenge()), "", foreignStamp));

    }

    @Test
    public void testMissingOrMalformedAltchaIsRefused() {

        final String stamp = stampThenWait();

        assertEquals(Verdict.CHALLENGE_FAILED, service.check(null, "", stamp));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check("", "", stamp));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check("not-base64!", "", stamp));
        assertEquals(Verdict.CHALLENGE_FAILED,
                service.check(Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8)), "", stamp));

    }

    @Test
    public void testSolvedChallengeCannotBeReplayed() throws Exception {

        final String altcha = solve(service.createChallenge());
        final String stamp = stampThenWait();

        assertEquals(Verdict.HUMAN, service.check(altcha, "", stamp));
        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", stamp));

    }

    @Test
    public void testExpiredChallengeIsRefused() throws Exception {

        // defi emis il y a 16 minutes, pour une validite de 15
        setNow(start.minus(Duration.ofMinutes(16)));
        final String altcha = solve(service.createChallenge());
        final String stamp = service.issueFormStamp();

        setNow(start);
        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", stamp));

    }

    @Test
    public void testChallengeSignedByAnotherInstanceIsRefused() throws Exception {

        final BotProtectionService other = new BotProtectionService();
        ReflectionTestUtils.setField(other, "altchaCost", 10);
        ReflectionTestUtils.setField(other, "altchaValidityMinutes", 15);
        ReflectionTestUtils.setField(other, "clock", Clock.fixed(start, ZoneOffset.UTC));

        final String altcha = solve(other.createChallenge());
        final String stamp = stampThenWait();

        assertEquals(Verdict.CHALLENGE_FAILED, service.check(altcha, "", stamp));

    }

    @Test
    public void testErrorMessagesNeverRevealTheHoneypot() {

        assertNotNull(BotProtectionService.errorMessage(Verdict.TOO_FAST));
        assertNotNull(BotProtectionService.errorMessage(Verdict.CHALLENGE_FAILED));
        assertThrows(IllegalArgumentException.class, () -> BotProtectionService.errorMessage(Verdict.HONEYPOT));

    }

}
