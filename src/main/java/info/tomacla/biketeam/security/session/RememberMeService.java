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

@Service
public class RememberMeService {

    @Value("${rememberme.key}")
    private String rememberMeKey;

    @Autowired
    private UserDetailsService userDetailsService;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    public Authentication getUserDetailsFromRememberMe(String rememberMe) {

        String[] valueTokens = this.decodeRememberMe(rememberMe);

        if (valueTokens.length != 3) {
            throw new RuntimeException("Remember me did not contain 3 tokens, but contained '" + Arrays.asList(valueTokens) + "'");
        }

        long tokenExpiryTime = this.getTokenExpiryTime(valueTokens);

        if (this.isTokenExpired(tokenExpiryTime)) {
            throw new RuntimeException("Remember me has expired (expired on '" + new Date(tokenExpiryTime) + "'; current time is '" + new Date() + "')");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(valueTokens[0]);

        if (userDetails == null) {
            throw new RuntimeException("Remember me does not match any existing account");
        }

        String expectedTokenSignature = this.makeTokenSignature(tokenExpiryTime, userDetails.getUsername(), userDetails.getPassword());
        if (!expectedTokenSignature.equals(valueTokens[2])) {
            throw new InvalidCookieException("Cookie token[2] contained signature '" + valueTokens[2] + "' but expected '" + expectedTokenSignature + "'");
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

    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        try {
            String data = username + ":" + tokenExpiryTime + ":" + password + ":" + rememberMeKey;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return new String(Hex.encode(digest.digest(data.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!", e);
        }
    }

}
