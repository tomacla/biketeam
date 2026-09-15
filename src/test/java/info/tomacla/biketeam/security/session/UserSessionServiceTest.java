package info.tomacla.biketeam.security.session;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Suppression des sessions HTTP d'un compte : sans elle, une session volee survivrait a la
 * reinitialisation du mot de passe (les sessions persistees ne sont jamais reconfrontees a la base).
 */
public class UserSessionServiceTest {

    @SuppressWarnings("unchecked")
    private FindByIndexNameSessionRepository<Session> repository() {
        return mock(FindByIndexNameSessionRepository.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private UserSessionService service(FindByIndexNameSessionRepository<Session> repository) {
        ObjectProvider provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repository);
        return new UserSessionService(provider);
    }

    private Map<String, Session> sessions(String... ids) {
        Map<String, Session> map = new LinkedHashMap<>();
        for (String id : ids) {
            MapSession session = new MapSession(id);
            map.put(id, session);
        }
        return map;
    }

    @Test
    public void testAllSessionsOfTheUserAreDeleted() {

        FindByIndexNameSessionRepository<Session> repository = repository();
        when(repository.findByPrincipalName("user-1")).thenReturn(sessions("s1", "s2", "s3"));

        int deleted = service(repository).invalidateAll("user-1");

        assertEquals(3, deleted);
        verify(repository).deleteById("s1");
        verify(repository).deleteById("s2");
        verify(repository).deleteById("s3");

    }

    @Test
    public void testNoSessionToDelete() {

        FindByIndexNameSessionRepository<Session> repository = repository();
        when(repository.findByPrincipalName("user-1")).thenReturn(Map.of());

        assertEquals(0, service(repository).invalidateAll("user-1"));
        verify(repository, never()).deleteById(anyString());

    }

    @Test
    public void testNullUserIdIsIgnored() {

        FindByIndexNameSessionRepository<Session> repository = repository();

        assertEquals(0, service(repository).invalidateAll(null));
        verify(repository, never()).findByPrincipalName(anyString());

    }

    /**
     * Le depot indexe n'existe que si spring-session est actif : son absence ne doit jamais faire
     * echouer une reinitialisation de mot de passe.
     */
    @Test
    public void testMissingRepositoryIsTolerated() {
        assertEquals(0, service(null).invalidateAll("user-1"));
    }

    @Test
    public void testRepositoryFailureIsSwallowed() {

        FindByIndexNameSessionRepository<Session> repository = repository();
        when(repository.findByPrincipalName("user-1")).thenThrow(new IllegalStateException("boom"));

        assertEquals(0, service(repository).invalidateAll("user-1"));

    }

}
