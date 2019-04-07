package commons.securelogging.jsonlogger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLoggerConfiguration {
    @Bean
    public JsonLoggingWrapper jsonLoggingWrapper() {
        return new JsonLoggingWrapper();
    }

    @Bean
    public LoggableAspect loggableAspect(JsonLoggingWrapper jasonLogger) {
        return new LoggableAspect(jasonLogger);
    }
}
