package sandbox.kafka.springkafka.cli;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${message.topic.name}")
    private String sandboxTopicName;

    @Value(value = "${partitioned.topic.name}")
    private String partitionedTopicName;

    @Value(value = "${filtered.topic.name}")
    private String filteredTopicName;

    @Value(value = "${stockTicker.topic.name}")
    private String stockTickerTopicName;

    @Value(value = "${stockTicker.topic.dlt}")
    private String stockTickerDeadLetterTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic messageTopic() {
        return new NewTopic(sandboxTopicName, 1, (short) 1);
    }

    @Bean
    public NewTopic stockTickerTopic() {
        return new NewTopic(stockTickerTopicName, 1, (short) 1);
    }

    @Bean
    public NewTopic stockTickerDeadLetterTopic() {
        return new NewTopic(stockTickerDeadLetterTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic partionedTopic() {
        return new NewTopic(partitionedTopicName, 6, (short) 1);
    }

    @Bean
    public NewTopic filteredTopic() {
        return new NewTopic(filteredTopicName, 1, (short) 1);
    }

}