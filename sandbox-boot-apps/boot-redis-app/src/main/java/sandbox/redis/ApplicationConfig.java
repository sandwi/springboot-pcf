package sandbox.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public StockTickerService getStockTickerService() {
        return new StockTickerService();
    }
}
