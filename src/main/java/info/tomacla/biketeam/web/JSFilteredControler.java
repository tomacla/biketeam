package info.tomacla.biketeam.web;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/jsf")
public class JSFilteredControler {

    @Autowired
    private Configuration freemarkerConfiguration;

    @Value("${ign.api.key:undefined}")
    private String ignApiKey;

    @Value("${custom.mbtiles.layer.url:undefined}")
    private String customMbTilesLayerURL;

    @ResponseBody
    @RequestMapping(value = "/leaflet-layers.js", method = RequestMethod.GET, produces = "text/javascript")
    public String getLeafletLayers(Model model) {

        Map<String, String> data = new HashMap<>();
        data.put("customMbTilesLayerURL", customMbTilesLayerURL.equals("undefined") ? null : customMbTilesLayerURL);
        data.put("ignApiKey", ignApiKey.equals("undefined") ? null : ignApiKey);

        return returnContent("__js_layers.ftlh", data);

    }

    private String returnContent(String templateName, Object data) {

        try {

            Template jsLayers = freemarkerConfiguration.getTemplate(templateName);
            StringWriter stringWriter = new StringWriter();
            jsLayers.process(data, stringWriter);
            String content = stringWriter.toString();

            return content;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to filter JS");
        }

    }

}
