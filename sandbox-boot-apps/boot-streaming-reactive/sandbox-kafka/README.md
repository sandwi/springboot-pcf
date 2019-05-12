# A basic Spring Boot and Kafka Application
## Install Kafka
Use [quick start guide](https://kafka.apache.org/quickstart) to install kafka.

1. Start Zookeeper:  
   ```bash
   cd $KAFKA_HOME
   bin/zookeeper-server-start.sh config/zookeeper.properties
  
2. Start Kafka Server:
   ```bash
   cd $KAFKA_HOME
   bin/kafka-server-start.sh config/server.properties

## Build code
After cloning this repo:  
```bash
cd boot-streaming-reactive
gradle clean build
```

## Run Boot Kafka App
```bash
cd boot-kafka
java -jar build/libs/sandbox-kafka-0.0.1-SNAPSHOT.jar
```

**Check the output from the app:**  

```bash
[main] INFO  sandbox.kafka.KafkaApplication - Started KafkaApplication in 2.15 seconds (JVM running for 2.803)
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in group 'bar': Hello, World!
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in group 'bar': Hello, World!
[main] INFO  sandbox.kafka.KafkaApplication - EXECUTING : command line runner

org.springframework.kafka.KafkaListenerEndpointContainer#4-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in filtered listener: Hello Sandbox!

[org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in group 'foo': Hello, World!
[org.springframework.kafka.KafkaListenerEndpointContainer#2-0-C-1] INFO  sandbox.kafka.MessageListener - Received: Hello, World! from partition: 0
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in group 'bar': Hello, World!
[kafka-producer-network-thread | producer-1] INFO  sandbox.kafka.MessageProducer - Sent message=[Hello, World!] with offset=[14]
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  sandbox.kafka.MessageListener - Received: Hello To Partitioned Topic! from partition: 2

[org.springframework.kafka.KafkaListenerEndpointContainer#4-0-C-1] INFO  sandbox.kafka.MessageListener - Received message in filtered listener: Hello Sandbox!
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  sandbox.kafka.MessageListener - Received: Hello To Partitioned Topic! from partition: 4
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  sandbox.kafka.MessageListener - Received: Hello To Partitioned Topic! from partition: 0

org.springframework.kafka.KafkaListenerEndpointContainer#5-0-C-1] INFO  o.s.k.l.KafkaMessageListenerContainer - partitions assigned: [stockTicker-0]
[org.springframework.kafka.KafkaListenerEndpointContainer#5-0-C-1] INFO  sandbox.kafka.MessageListener - Received stockTicker: StockTicker(ticker=AAPL, price=120.12)
```
