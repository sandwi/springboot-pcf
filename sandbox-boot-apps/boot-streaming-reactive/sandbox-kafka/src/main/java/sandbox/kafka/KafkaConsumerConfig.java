package sandbox.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
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

    public ConsumerFactory<String, StockTicker> stockTickerConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "stockTicker");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(StockTicker.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockTicker> stockTickerKafkaListenerContainerFactory(
            KafkaTemplate<Object, Object> dltKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StockTicker> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stockTickerConsumerFactory());
//        factory.setErrorHandler(new SeekToCurrentErrorHandler(
//                new DeadLetterPublishingRecoverer(dltKafkaTemplate), 3)); // dead-letter after 3 tries
        factory.setErrorHandler(new ErrorHandler() {
            @Override
            public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
                String s = thrownException.getMessage().split("Error deserializing key/value for partition ")[1].split(". If needed, please seek past the record to continue consumption.")[0];
                String topics = s.split("-")[0];
                int offset = Integer.valueOf(s.split("offset ")[1]);
                int partition = Integer.valueOf(s.split("-")[1].split(" at")[0]);

                TopicPartition topicPartition = new TopicPartition(topics, partition);
                log.info("Skipping stockTicker -" + partition + " offset " + offset);
                consumer.seek(topicPartition, offset + 1);
                log.info("## Exiting ErrorHandler().");
            }

            @Override
            public void handle(Exception e, ConsumerRecord<?, ?> consumerRecord) {
                log.info("Exception: " + e.getMessage());
                e.printStackTrace();
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
    public KafkaTemplate<Object, Object> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    @Bean
    public SeekToCurrentErrorHandler stockTickerErrorHandler() {
        return new SeekToCurrentErrorHandler((record, exception) -> {
            // recover after 3 failures - e.g. send to a dead-letter topic
            log.info("StockTicker Record: " + record);
            log.info("Exception: " + exception.getMessage());
        }, 3);
    }

//    @Bean
//    public RemainingRecordsErrorHandler stockTickerRemainingRecordsErrorHandler() {
//    return new RemainingRecordsErrorHandler ((e, recordList, consumer) -> {
//
//            });
//    }

}
