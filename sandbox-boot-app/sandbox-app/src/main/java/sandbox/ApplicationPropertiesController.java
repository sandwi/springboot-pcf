package sandbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@RefreshScope
@RestController
public class ApplicationPropertiesController {

    private Environment env;
    private Properties applicationProperties;

    @Value("${sandbox.testProp:unknown}")
    private String testKey;

    @Autowired
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    @Autowired
    @Qualifier("applicationProps")
    public void setApplicationProperties(Properties props) {
        this.applicationProperties = props;
    }

    @RequestMapping("/test-key")
    @ResponseBody
    String getTestKeyValue() {
        return this.testKey;
    }

    @GetMapping("/config-value")
    @ResponseBody
    String getConfigValue(@RequestParam("key") String key) {
        return this.env.getProperty(key);
    }

    @GetMapping(value = "/all-configs", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getAll() {
        Map<String, String> map = new HashMap<>();
        this.applicationProperties.forEach((k,v) -> map.put(k.toString(), v.toString()));
        String mapAsJson = null;
        try {
            mapAsJson = new ObjectMapper().writeValueAsString(map);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return mapAsJson;
    }
}
