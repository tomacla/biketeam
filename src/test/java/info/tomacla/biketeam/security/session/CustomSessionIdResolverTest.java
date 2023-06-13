package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomSessionIdResolverTest {


    @Test
    public void test() {

        UrlService urlService = Mockito.mock(UrlService.class);

        final CustomSessionIdResolver c = new CustomSessionIdResolver(urlService);

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final List<String> strings = c.resolveSessionIds(request);

        assertTrue(strings.isEmpty());


    }

}
