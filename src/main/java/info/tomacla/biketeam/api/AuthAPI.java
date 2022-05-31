package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.TokenDTO;
import info.tomacla.biketeam.api.dto.UserDTO;
import info.tomacla.biketeam.security.session.CustomUserDetailsService;
import info.tomacla.biketeam.security.session.RememberMeService;
import info.tomacla.biketeam.security.session.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthAPI extends AbstractAPI {

    @Autowired
    private SSOService ssoService;

    @Autowired
    private RememberMeService rememberMeService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping(path = "/me", produces = "application/json")
    public UserDTO whoami(Principal principal) {
        return UserDTO.valueOf(getUserFromPrincipal(principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN)));
    }

    @PostMapping(path = "/sso", consumes = "text/plain", produces = "application/json")
    public TokenDTO exchangeSSOToken(@RequestBody String ssoToken) {

        final Optional<String> sessionId = ssoService.getSessionIdFromSSOToken(ssoToken);
        final Optional<String> rememberMe = ssoService.getRememberMeFromSSOToken(ssoToken);

        return TokenDTO.valueOf(sessionId.get(), rememberMe.get());
    }

    @PostMapping(path = "/refresh", consumes = "text/plain", produces = "application/json")
    public TokenDTO refreshSessionId(@RequestBody String rememberMe) {

        Authentication userDetailsFromRememberMe = rememberMeService.getUserDetailsFromRememberMe(rememberMe);

        userDetailsFromRememberMe = this.authenticationManager.authenticate(userDetailsFromRememberMe);
        SecurityContextHolder.getContext().setAuthentication(userDetailsFromRememberMe);

        final String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

        return TokenDTO.valueOf(sessionId, rememberMe);

    }


}
