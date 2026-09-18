package info.tomacla.biketeam.web;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new WebExceptionHandler())
            .build();

    @Test
    void responseStatusExceptionProducesItsStatus() throws Exception {
        // et non la redirection vers l'accueil : c'est ce qui permet le 404 d'une equipe inconnue
        mockMvc.perform(get("/response-status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingStaticResourceIsNotFound() throws Exception {
        // /.env, *.js.map... demandes par les scanners et les devtools
        mockMvc.perform(get("/missing-resource"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethodIsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/boom"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void otherExceptionsRedirectToRoot() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void committedResponseIsLeftAsIs() throws Exception {
        // pas de sendRedirect sur une reponse deja envoyee, sinon IllegalStateException
        mockMvc.perform(get("/boom-after-commit"))
                .andExpect(status().isOk());
    }

    @Controller
    static class ThrowingController {

        @GetMapping("/response-status")
        public String responseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown team nope");
        }

        @GetMapping("/missing-resource")
        public String missingResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "missing-resource");
        }

        @GetMapping("/boom")
        public String boom() {
            throw new IllegalStateException("boom");
        }

        @GetMapping("/boom-after-commit")
        public String boomAfterCommit(HttpServletResponse response) throws Exception {
            response.getWriter().write("deja parti");
            response.flushBuffer();
            throw new IllegalStateException("boom");
        }

    }

}
