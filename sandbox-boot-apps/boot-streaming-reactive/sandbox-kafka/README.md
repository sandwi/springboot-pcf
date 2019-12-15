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
After cloning this repo, build the app. Gradle is used for builds, hence gradle should be intalled on your machine.

There are mutliple boot applications:
1. HelloWorld: Simplest possible boot-kafka app, runs with default servlet container.
1. CLI Java App: A boot CLI boot kafka app, runs as Java CLI app.

By default helloworld version is started.

### Build HelloWorld

This is the default, no changes are required to build/run this version. 
 
```bash
cd boot-streaming-reactive
gradle clean build
```

### Build CLI App

1. Modify build.gradle, to change Spring Boot main class, uncomment CLI main class and comment out helloworld main class:

```bash
cd boot-streaming-reactive
```
Use any editor to modify build.gradle (e.g. vi):
```bash
    mainClassName = 'KafkaApplication'
//    mainClassName = 'KafkaApplication'

```
 
```bash
gradle clean build
``` 

## Run Boot Kafka App
Assuming Kafka has been started.

### HelloWorld version

In a new terminal shell, start the Spring Boot app:
```bash
cd boot-kafka
java -jar build/libs/sandbox-kafka-0.0.1-SNAPSHOT.jar --spring.config.name=application-hw
```

This will start Spring Boot App on port 9000. This version exposes REST endpoint to which you can post messages.
In another new terminal shell, post message using curl:

```bash
curl -X POST -F 'message={"ticker":AAPL, "price":21}' http://localhost:9000/kafka/publish
```

The out in the terminal where Spring Boot Application is running should look like:
```bash
19:48:38.982 [http-nio-9000-exec-2] INFO  Producer - #### -> Producing message -> {"ticker":AAPL, "price":21"
19:48:38.986 [org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1] INFO  Consumer - #### -> Consumed message -> {"ticker":AAPL, "price":21}
```

### CLI App Version
In a new terminal shell, start the Spring Boot app:
```bash
cd boot-kafka
java -jar build/libs/sandbox-kafka-0.0.1-SNAPSHOT.jar --spring.config.name=application-cli
```

Spring Boot App will start and run to completion.

**Check the output from the app:**  

```bash
[main] INFO  KafkaApplication - Started KafkaApplication in 2.15 seconds (JVM running for 2.803)
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  MessageListener - Received message in group 'bar': Hello, World!
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  MessageListener - Received message in group 'bar': Hello, World!
[main] INFO  KafkaApplication - EXECUTING : command line runner

org.springframework.kafka.KafkaListenerEndpointContainer#4-0-C-1] INFO  MessageListener - Received message in filtered listener: Hello Sandbox!

[org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1] INFO  MessageListener - Received message in group 'foo': Hello, World!
[org.springframework.kafka.KafkaListenerEndpointContainer#2-0-C-1] INFO  MessageListener - Received: Hello, World! from partition: 0
[org.springframework.kafka.KafkaListenerEndpointContainer#1-0-C-1] INFO  MessageListener - Received message in group 'bar': Hello, World!
[kafka-producer-network-thread | producer-1] INFO  MessageProducer - Sent message=[Hello, World!] with offset=[14]
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  MessageListener - Received: Hello To Partitioned Topic! from partition: 2

[org.springframework.kafka.KafkaListenerEndpointContainer#4-0-C-1] INFO  MessageListener - Received message in filtered listener: Hello Sandbox!
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  MessageListener - Received: Hello To Partitioned Topic! from partition: 4
[org.springframework.kafka.KafkaListenerEndpointContainer#3-0-C-1] INFO  MessageListener - Received: Hello To Partitioned Topic! from partition: 0

org.springframework.kafka.KafkaListenerEndpointContainer#5-0-C-1] INFO  o.s.k.l.KafkaMessageListenerContainer - partitions assigned: [stockTicker-0]
[org.springframework.kafka.KafkaListenerEndpointContainer#5-0-C-1] INFO  MessageListener - Received stockTicker: StockTicker(ticker=AAPL, price=120.12)
```
