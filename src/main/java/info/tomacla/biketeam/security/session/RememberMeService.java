package info.tomacla.biketeam.security.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * Lecture d'un cookie remember-me presente hors navigateur (application mobile).
 * <p>
 * Deux formats sont acceptes :
 * <ul>
 *     <li>3 jetons {@code username:expiry:signature}, signature MD5 : cookies historiques,
 *     emis par les versions anterieures de Spring Security ;</li>
 *     <li>4 jetons {@code username:expiry:ALGORITHME:signature} : format emis par
 *     TokenBasedRememberMeServices depuis Spring Security 6, ou l'algorithme par defaut
 *     (DEFAULT_ENCODING_ALGORITHM) est SHA256.</li>
 * </ul>
 */
@Service
public class RememberMeService {

    /**
     * Nom de la constante d'enumeration RememberMeTokenAlgorithm, tel qu'ecrit dans le cookie.
     */
    private static final String ALGORITHM_MD5 = "MD5";

    @Value("${rememberme.key}")
    private String rememberMeKey;

    @Autowired
    private UserDetailsService userDetailsService;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public Authentication getUserDetailsFromRememberMe(String rememberMe) {

        String[] valueTokens = this.decodeRememberMe(rememberMe);

        if (valueTokens.length != 3 && valueTokens.length != 4) {
            throw new RuntimeException("Remember me did not contain 3 or 4 tokens, but contained '" + Arrays.asList(valueTokens) + "'");
        }

        long tokenExpiryTime = this.getTokenExpiryTime(valueTokens);

        if (this.isTokenExpired(tokenExpiryTime)) {
            throw new RuntimeException("Remember me has expired (expired on '" + new Date(tokenExpiryTime) + "'; current time is '" + new Date() + "')");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(valueTokens[0]);

        if (userDetails == null) {
            throw new RuntimeException("Remember me does not match any existing account");
        }

        // le dernier jeton est toujours la signature ; l'avant-dernier porte le nom de
        // l'algorithme dans le format a 4 jetons (absent du format historique, alors MD5)
        final String signature = valueTokens[valueTokens.length - 1];
        final String algorithmName = (valueTokens.length == 4) ? valueTokens[2] : ALGORITHM_MD5;

        String expectedTokenSignature = this.makeTokenSignature(tokenExpiryTime,
                userDetails.getUsername(), userDetails.getPassword(), getDigestAlgorithm(algorithmName));

        if (!MessageDigest.isEqual(
                expectedTokenSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidCookieException("Cookie signature '" + signature + "' does not match the expected one");
        }

        this.userDetailsChecker.check(userDetails);

        return createSuccessfulAuthentication(userDetails);


    }

    protected Authentication createSuccessfulAuthentication(UserDetails user) {
        return new RememberMeAuthenticationToken(rememberMeKey, user, user.getAuthorities());
    }

    protected boolean isTokenExpired(long tokenExpiryTime) {
        return tokenExpiryTime < System.currentTimeMillis();
    }

    private long getTokenExpiryTime(String[] tokens) {
        try {
            return Long.valueOf(tokens[1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unable to get expiry time from remember me (contained '" + tokens[1] + "')", e);
        }
    }

    protected String[] decodeRememberMe(String rememberMe) throws InvalidCookieException {

        try {

            for (int j = 0; j < rememberMe.length() % 4; ++j) {
                rememberMe = rememberMe + "=";
            }

            String cookieAsPlainText = new String(Base64.getDecoder().decode(rememberMe.getBytes()));
            String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, ":");

            for (int i = 0; i < tokens.length; ++i) {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            }

            return tokens;

        } catch (Exception e) {
            throw new RuntimeException("Failed to decode remember me '" + rememberMe + "'", e);
        }


    }

    protected String makeTokenSignature(long tokenExpiryTime, String username, String password, String digestAlgorithm) {
        try {
            String data = username + ":" + tokenExpiryTime + ":" + password + ":" + rememberMeKey;
            MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
            return new String(Hex.encode(digest.digest(data.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No " + digestAlgorithm + " algorithm available!", e);
        }
    }

    /**
     * Le cookie porte le nom de la constante d'enumeration de Spring Security (MD5 ou SHA256),
     * qui n'est pas le nom de l'algorithme JCA (MD5 ou SHA-256).
     */
    protected String getDigestAlgorithm(String algorithmName) {
        return ALGORITHM_MD5.equals(algorithmName) ? "MD5" : "SHA-256";
    }

}
