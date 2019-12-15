package sandbox.kafka.springkafka.cli;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    public ConsumerFactory<String, String> consumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> fooKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("foo"));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> barKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("bar"));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> headersKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("headers"));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> partitionsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("partitions"));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> filterKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("filter"));
        factory.setRecordFilterStrategy(record -> record.value()
                .contains("World"));
        return factory;
    }

    //@Bean
    public ConsumerFactory<String, StockTicker> stockTickerConsumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        consumerProps.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        consumerProps.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        consumerProps.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedStockTickerProvider.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "StockTicker");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "sandbox.kafka");

        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "stockTicker");
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockTicker> stockTickerKafkaListenerContainerFactory(
            KafkaTemplate<Object, Object> dltKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StockTicker> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stockTickerConsumerFactory());
//        factory.setErrorHandler(new SeekToCurrentErrorHandler(
//                new DeadLetterPublishingRecoverer(dltKafkaTemplate), 3)); // dead-letter after 3 tries

        factory.setErrorHandler((t, r) -> {
            if (r.value() == null && t.getCause() instanceof DeserializationException) {
                DeserializationException de = (DeserializationException) t.getCause();
                Headers headers = de.getHeaders();
                log.info("Headers: " + headers);
                log.info("Data: " + new String(de.getData()));
            }
            else if (r.key() == null && t.getCause() instanceof DeserializationException) {
                DeserializationException de = (DeserializationException) t.getCause();
                log.info("Keys: " + de.getMessage());
            }
        });
        return factory;
    }

//    @Bean
//    public RecordMessageConverter converter() {
//        return new StringJsonMessageConverter();
//    }

    @Bean
    public ProducerFactory<Object, Object> dltProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Object, Object> dltKafkaTemplate(ProducerFactory<Object, Object> dltProducerFactory) {
        return new KafkaTemplate<>(dltProducerFactory);
    }
}
