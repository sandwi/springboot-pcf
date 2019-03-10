package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;

import java.util.Iterator;
import java.util.Properties;

@Configuration
@Slf4j
public class ApplicationPropertiesConfig {

    private Environment env;

    @Autowired
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    //Load all the properties of the server and put them into a java Properties obj
    @Bean(name = "applicationProps")
    public Properties applicationProperties() {
        final Properties properties = new Properties();
        for(Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource propertySource = (PropertySource) it.next();
            if (propertySource instanceof PropertiesPropertySource) {
                log.info("Adding all properties contained in " + propertySource.getName());
                properties.putAll(((MapPropertySource) propertySource).getSource());
            }
            if (propertySource instanceof CompositePropertySource){
                properties.putAll(getPropertiesInCompositePropertySource((CompositePropertySource) propertySource));
            }
        }
        return properties;
    }

    private Properties getPropertiesInCompositePropertySource(CompositePropertySource compositePropertySource){
        final Properties properties = new Properties();
        compositePropertySource.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                log.info("Adding all properties contained in " + propertySource.getName());
                properties.putAll(((MapPropertySource) propertySource).getSource());
            }
            if (propertySource instanceof CompositePropertySource)
                properties.putAll(getPropertiesInCompositePropertySource((CompositePropertySource) propertySource));
        });
        return properties;
    }

}
