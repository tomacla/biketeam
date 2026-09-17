package info.tomacla.biketeam.web;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La page d'erreur est le dernier filet de securite : si son rendu echoue, l'erreur d'origine
 * est remplacee par une erreur de template. On verifie donc qu'elle se rend pour chaque forme
 * de modele que BasicErrorController peut lui passer, modele vide inclus.
 */
class ErrorTemplateTest {

    private final Configuration configuration = configuration();

    @Test
    void notFoundRendersItsOwnMessage() throws Exception {
        String html = render(model(404));
        assertTrue(html.contains("Page introuvable"), html);
        assertTrue(html.contains("Erreur 404"), html);
    }

    @Test
    void forbiddenRendersItsOwnMessage() throws Exception {
        String html = render(model(403));
        assertTrue(html.contains("Accès refusé"), html);
        assertTrue(html.contains("Erreur 403"), html);
    }

    @Test
    void otherStatusRendersTheGenericMessage() throws Exception {
        String html = render(model(500));
        assertTrue(html.contains("Une erreur est survenue"), html);
        assertTrue(html.contains("Erreur 500"), html);
    }

    @Test
    void emptyModelStillRenders() throws Exception {
        // acces direct a /error : DefaultErrorAttributes renvoie un statut hors plage HTTP
        String html = render(new HashMap<>());
        assertTrue(html.contains("Une erreur est survenue"), html);
        assertTrue(!html.contains("Erreur "), html);
    }

    private Map<String, Object> model(int status) {
        Map<String, Object> model = new HashMap<>();
        model.put("timestamp", new java.util.Date());
        model.put("status", status);
        model.put("error", "Whatever");
        model.put("path", "/n-peloton/inexistant");
        return model;
    }

    private String render(Map<String, Object> model) throws Exception {
        StringWriter out = new StringWriter();
        configuration.getTemplate("error.ftlh").process(model, out);
        return out.toString();
    }

    private static Configuration configuration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
        configuration.setClassForTemplateLoading(ErrorTemplateTest.class, "/templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return configuration;
    }

}
