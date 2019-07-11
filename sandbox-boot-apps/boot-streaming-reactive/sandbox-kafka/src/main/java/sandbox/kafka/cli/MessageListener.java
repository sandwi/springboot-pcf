package sandbox.kafka.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageListener {

    private CountDownLatch latch = new CountDownLatch(3);

    private CountDownLatch partitionLatch = new CountDownLatch(2);

    private CountDownLatch filterLatch = new CountDownLatch(2);

    private CountDownLatch stockTickerLatch = new CountDownLatch(1);

    public MessageListener() {}

    public void wait(int time,TimeUnit unit) throws InterruptedException {
        this.latch.await(time, unit);
    }

    public void partitionTopicWait(int time,TimeUnit unit) throws InterruptedException {
        this.partitionLatch.await(time, unit);
    }

    public void filterTopicWait(int time,TimeUnit unit) throws InterruptedException {
        this.filterLatch.await(time, unit);
    }

    public void stockTickerWait(int time,TimeUnit unit) throws InterruptedException {
        this.stockTickerLatch.await(time, unit);
    }

    @KafkaListener(topics = "${message.topic.name}", groupId = "foo", containerFactory = "fooKafkaListenerContainerFactory")
    public void listenGroupFoo(String message) {
        log.info("Received message in group 'foo': " + message);
        latch.countDown();
    }

    @KafkaListener(topics = "${message.topic.name}", groupId = "bar", containerFactory = "barKafkaListenerContainerFactory")
    public void listenGroupBar(String message) {
        log.info("Received message in group 'bar': " + message);
        latch.countDown();
    }

    @KafkaListener(topics = "${message.topic.name}", containerFactory = "headersKafkaListenerContainerFactory")
    public void listenWithHeaders(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        log.info("Received: " + message + " from partition: " + partition);
        latch.countDown();
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "${partitioned.topic.name}", partitions = { "0", "2", "4" }))
    public void listenToPartition(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        log.info("Received: " + message + " from partition: " + partition);
        this.partitionLatch.countDown();
    }

    @KafkaListener(topics = "${filtered.topic.name}", containerFactory = "filterKafkaListenerContainerFactory")
    public void listenWithFilter(String message) {
        log.info("Received message in filtered listener: " + message);
        this.filterLatch.countDown();
    }

    @KafkaListener(topics = "${stockTicker.topic.name}", containerFactory = "stockTickerKafkaListenerContainerFactory")
//            errorHandler = "stockTickerErrorHandler")
    public void stockTickerListener(StockTicker stockTicker) {
        log.info("Received stockTicker: " + stockTicker);
        this.stockTickerLatch.countDown();
    }


}
