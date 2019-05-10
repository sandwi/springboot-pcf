package sandbox.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
@Profile({"local"})
public class LocalRedisClientConfig {
    @Value("${redis.hostname}")
    private String hostname;
    @Value("${redis.port}")
    private Integer port;
    @Value("${redis.connectionTimeout}")
    private Integer redisConnectionTimeout;
    @Value("${redis.readTimeout}")
    private Integer redisReadTimeout;

    @Bean(name="redisFactory")
    public RedisConnectionFactory redisFactory() {
        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder()
                .readTimeout(Duration.ofMillis(redisReadTimeout))
                .connectTimeout(Duration.ofMillis(redisConnectionTimeout))
                .usePooling()
                .build();
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(hostname);
        standaloneConfiguration.setPort(port);
        return new JedisConnectionFactory(standaloneConfiguration, clientConfiguration);
    }

}