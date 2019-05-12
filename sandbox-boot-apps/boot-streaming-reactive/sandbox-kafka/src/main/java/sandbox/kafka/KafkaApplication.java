package sandbox.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class KafkaApplication implements CommandLineRunner {

    @Autowired
    private MessageProducer producer;
    @Autowired
    private MessageListener listener;

    public static void main(String... args) {
        log.info("STARTING THE APPLICATION");
        ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);
        context.close();
        log.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }

        try {
            producer.sendMessage("Hello, World!");
            listener.wait(10, TimeUnit.SECONDS);

            /*
             * Sending message to a topic with 5 partition,
             * each message to a different partition. But as per
             * listener configuration, only the messages from
             * partition 0, 2 and 4 will be consumed.
             */
            for (int i = 0; i < 5; i++) {
                producer.sendMessageToPartition("Hello To Partitioned Topic!", i);
            }
            listener.partitionTopicWait(10, TimeUnit.SECONDS);

            /*
             * Sending message to 'filtered' topic. As per listener
             * configuration,  all messages with char sequence
             * 'World' will be discarded.
             */
            producer.sendMessageToFiltered("Hello Sandbox!");
            producer.sendMessageToFiltered("Hello World!");
            listener.filterTopicWait(10, TimeUnit.SECONDS);

            /*
             * Sending message to 'stockTicker' topic. This will send
             * and receive a java object with the help of
             * stockTickerKafkaListenerContainerFactory.
             */
            StockTicker ticker = new StockTicker();
            ticker.setTicker("AAPL");
            ticker.setPrice("120.12");
            producer.sendStockTickerMessage(ticker);
            listener.stockTickerWait(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
