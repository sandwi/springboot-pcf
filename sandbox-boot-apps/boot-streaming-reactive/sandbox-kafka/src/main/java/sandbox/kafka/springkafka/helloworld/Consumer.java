package sandbox.kafka.springkafka.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class Consumer {


    @KafkaListener(topics = "bank-accounts", groupId = "sandbox-group-1")
    public void consume(String message) throws IOException {
        log.info(String.format("#### -> Consumed message -> %s", message));
    }
}
