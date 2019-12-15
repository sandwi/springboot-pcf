package sandbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RefreshScope
@RestController
public class ApplicationPropertiesController {

    @Value("${CF_INSTANCE_GUID:UNKNOWN}")
    private String guid;
    @Value("${CF_INSTANCE_ADDR:UNKNOWN}")
    private String addr;
    @Value("${CF_INSTANCE_INDEX:UNKNOWN}")
    private String index;

    @Value("${sandbox.testProp:unknown}")
    private String testKey;

    private Environment env;
    private Properties applicationProperties;
    private Properties applicationPropertiesNotRefreshable;

    @Autowired
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    @Autowired
    @Qualifier("applicationProperties")
    public void setApplicationProperties(Properties props) {
        this.applicationProperties = props;
    }

    @Autowired
    @Qualifier("applicationPropertiesNotRefreshable")
    public void setApplicationPropertiesNotRefreshable(Properties props) {
        this.applicationPropertiesNotRefreshable = props;
    }

    @GetMapping(value = "/test-key", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getTestKeyValue() {
        Map<String, String> map = new HashMap<>();
        map.put(testKey, this.testKey);
        return this.testKey;
    }

    @GetMapping(value = "/config-value", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getConfigValue(@RequestParam("key") String key) {
        Map<String, String> map = new HashMap<>();
        map.put(key, this.env.getProperty(key));
        return getMapAsJson(map);
    }

    @GetMapping(value = "/all-configs", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getAll() {
        Map<String, String> map = new HashMap<>();
        ApplicationPropertiesReader.applicationProperties(env).forEach((k,v) -> map.put(k.toString(), v.toString()));
        return getMapAsJson(map);
    }

    @GetMapping(value = "/app-properties", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getApplicationProperties() {
        Map<String, String> map = new HashMap<>();
        applicationProperties.forEach((k,v) -> map.put(k.toString(), v.toString()));
        return getMapAsJson(map);
    }

    @GetMapping(value = "/app-properties2", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    String getApplicationPropertiesNotRefreshable() {
        Map<String, String> map = new HashMap<>();
        applicationPropertiesNotRefreshable.forEach((k,v) -> map.put(k.toString(), v.toString()));
        return getMapAsJson(map);
    }

    private String getMapAsJson(Map<String, String> map) {
        map.put("CF_INSTANCE_GUID", guid);
        map.put("CF_INSTANCE_INDEX", index);
        map.put("CF_INSTANCE_ADDR", addr);

        String mapAsJson = null;
        try {
            mapAsJson = new ObjectMapper().writeValueAsString(map);
        } catch(JsonProcessingException e) {
            e.printStackTrace();
        }
        return mapAsJson;
    }
}
