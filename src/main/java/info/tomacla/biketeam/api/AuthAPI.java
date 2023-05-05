package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.TokenDTO;
import info.tomacla.biketeam.api.dto.UserDTO;
import info.tomacla.biketeam.security.session.RememberMeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthAPI extends AbstractAPI {

    @Autowired
    private RememberMeService rememberMeService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping(path = "/me", produces = "application/json")
    public UserDTO whoami(Principal principal) {
        return UserDTO.valueOf(getUserFromPrincipal(principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN)));
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
