package sandbox.redis.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static java.util.Collections.singletonMap;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;


@Configuration
@EnableCaching
@Profile({"!local"})
public class RedisConfig extends AbstractCloudConfig {


    /**
     * Connect to the only available Redis service
     */
    @Bean(name="redisFactory")
    public RedisConnectionFactory redisFactory() {
        return connectionFactory().redisConnectionFactory();
    }


    @Bean
    RedisTemplate<String, Double> redisTemplate() {
        RedisTemplate<String, Double> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisFactory());
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }

    @Bean
    public RedisCacheManager expiringCacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(1))
                .disableCachingNullValues();

        RedisCacheManager cm = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(singletonMap("predefined", config))
                .transactionAware()
                .build();

        return cm;
    }

}