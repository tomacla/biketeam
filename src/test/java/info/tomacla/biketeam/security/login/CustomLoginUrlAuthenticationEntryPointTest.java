package info.tomacla.biketeam.security.login;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomLoginUrlAuthenticationEntryPointTest {

    @Test
    public void test() {
        CustomLoginUrlAuthenticationEntryPoint c = new CustomLoginUrlAuthenticationEntryPoint();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");

        final String redirectUrlToLoginPage = c.buildRedirectUrlToLoginPage(request, null, null);

        assertEquals(redirectUrlToLoginPage, "http://localhost/login?requestUri=/test");

    }


    @Test
    public void testNull() {
        CustomLoginUrlAuthenticationEntryPoint c = new CustomLoginUrlAuthenticationEntryPoint();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(null);

        final String redirectUrlToLoginPage = c.buildRedirectUrlToLoginPage(request, null, null);

        assertEquals(redirectUrlToLoginPage, "http://localhost/login");

    }

}
