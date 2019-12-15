package sandbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;

import java.util.Iterator;
import java.util.Properties;

@RefreshScope
@Configuration
@Slf4j
public class ApplicationPropertiesConfig {
    private Environment env;

    @Autowired
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    @Bean(name = "applicationProperties")
    @RefreshScope
    Properties applicationProperties() {
        return ApplicationPropertiesReader.applicationProperties(env);
    }

    @Bean(name = "applicationPropertiesNotRefreshable")
    Properties applicationPropertiesNotRefreshable() {
        return ApplicationPropertiesReader.applicationProperties(env);
    }
}
