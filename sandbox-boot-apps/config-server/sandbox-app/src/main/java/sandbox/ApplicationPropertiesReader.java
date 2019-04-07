package sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.*;

import java.util.Iterator;
import java.util.Properties;

@Slf4j
public class ApplicationPropertiesReader {

    //Load all the properties of the server and put them into a java Properties obj
    public static Properties applicationProperties(Environment env) {
        final Properties properties = new Properties();
        for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource propertySource = (PropertySource) it.next();
            if (propertySource instanceof PropertiesPropertySource) {
                log.debug("Adding all properties contained in " + propertySource.getName());
                properties.putAll(((MapPropertySource) propertySource).getSource());
            }
            if (propertySource instanceof CompositePropertySource){
                properties.putAll(getPropertiesInCompositePropertySource((CompositePropertySource) propertySource));
            }
        }
        return properties;
    }

    private static Properties getPropertiesInCompositePropertySource(CompositePropertySource compositePropertySource){
        final Properties properties = new Properties();
        compositePropertySource.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                log.debug("Adding all properties contained in " + propertySource.getName());
                properties.putAll(((MapPropertySource) propertySource).getSource());
            }
            if (propertySource instanceof CompositePropertySource)
                properties.putAll(getPropertiesInCompositePropertySource((CompositePropertySource) propertySource));
        });
        return properties;
    }
}
