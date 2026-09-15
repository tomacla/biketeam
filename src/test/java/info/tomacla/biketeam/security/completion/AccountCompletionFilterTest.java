package info.tomacla.biketeam.security.completion;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountCompletionFilterTest {

    private AccountCompletionFilter filter;
    private AccountCompletionService accountCompletionService;

    @BeforeEach
    public void setUp() throws Exception {
        filter = new AccountCompletionFilter();
        accountCompletionService = mock(AccountCompletionService.class);
        Field f = AccountCompletionFilter.class.getDeclaredField("accountCompletionService");
        f.setAccessible(true);
        f.set(filter, accountCompletionService);

        SecurityContextHolder.clearContext();
    }

    private void authenticateAsUser() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.authenticated("user-1", null, authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    public void testAnonymousPassesThrough() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some/page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());

    }

    @Test
    public void testCompleteAccountPassesThrough() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(true);
        when(accountCompletionService.isComplete(any(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);

    }

    @Test
    public void testIncompleteAccountRedirectedToAccountComplete() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(true);
        when(accountCompletionService.isComplete(any(), any())).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertEquals("/account/complete", response.getRedirectedUrl());

    }

    @Test
    public void testEveryWhitelistEntryPassesThrough() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(true);
        when(accountCompletionService.isComplete(any(), any())).thenReturn(false);

        for (String pattern : AccountCompletionFilter.WHITELIST_PATTERNS) {
            String path = pattern.replace("**", "anything").replace("*", "anything");
            // les patterns sans wildcard restent tels quels (ex: "/logout", "/error")
            if (path.contains("*")) {
                path = path.replace("*", "x");
            }

            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            request.setServletPath(path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            verify(chain, description("pattern " + pattern + " -> path " + path + " should pass through"))
                    .doFilter(request, response);
        }

    }

    @Test
    public void testXhrRequestReceivesJson403InsteadOfRedirect() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(true);
        when(accountCompletionService.isComplete(any(), any())).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("account_completion_required"));
        assertNull(response.getRedirectedUrl());

    }

    @Test
    public void testJsonAcceptHeaderReceivesJson403() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(true);
        when(accountCompletionService.isComplete(any(), any())).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader("Accept", "application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());

    }

    @Test
    public void testEnforcementDisabledPassesThrough() throws Exception {

        authenticateAsUser();
        when(accountCompletionService.enforcementEnabled()).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(accountCompletionService, never()).isComplete(any(), any());

    }

}
