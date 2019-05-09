package sandbox.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class KafkaApplication {

    @Autowired
    private static MessageProducer producer;
    @Autowired
    private static MessageListener listener;

    public static void main(String... args) {

        try {
            ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);


            producer.sendMessage("Hello, World!");
            listener.wait(10, TimeUnit.SECONDS);

            /*
             * Sending message to a topic with 5 partition,
             * each message to a different partition. But as per
             * listener configuration, only the messages from
             * partition 0 and 3 will be consumed.
             */
            for (int i = 0; i < 5; i++) {
                producer.sendMessageToPartion("Hello To Partioned Topic!", i);
            }
            listener.partitionTopicWait(10, TimeUnit.SECONDS);

            /*
             * Sending message to 'filtered' topic. As per listener
             * configuration,  all messages with char sequence
             * 'World' will be discarded.
             */
            producer.sendMessageToFiltered("Hello Baeldung!");
            producer.sendMessageToFiltered("Hello World!");
            listener.filterTopicWait(10, TimeUnit.SECONDS);

            /*
             * Sending message to 'greeting' topic. This will send
             * and recieved a java object with the help of
             * greetingKafkaListenerContainerFactory.
             */
            StockTicker ticker = new StockTicker();
            ticker.setTicker("AAPL");
            ticker.setPrice("120.12");
            producer.sendStockTickerMessage(ticker);
            listener.stockTickerWait(10, TimeUnit.SECONDS);

            context.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
