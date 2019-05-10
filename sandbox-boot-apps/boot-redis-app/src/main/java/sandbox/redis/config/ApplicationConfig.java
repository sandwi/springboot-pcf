package sandbox.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sandbox.redis.service.StockTickerService;

@Configuration
public class ApplicationConfig {
    @Bean
    public StockTickerService getStockTickerService() {
        return new StockTickerService();
    }
}
