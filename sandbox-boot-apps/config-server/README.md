## Overview

Cloud-Native apps should follow 12 factor application architecture principles and externalizing configuration is one of them. More information on 12 factors is available at https://12factor.net/config.

Configurations for multiple apps/environments can be kept in a single Git repository and Pivotal CF's config-server service can be spun to serve configurations bcked by a git repository. Upon application push, PCF injects the location of a configured config-server in the environment and the spring-boot app contacts the config-server to pickup its own configurations.

## Details

### Create a config-server service in the org/space

Create an instance of Config Server using a command like:

```
cf create-service p-config-server standard my-config-server -c config_server.json
```

In the above command, the config_server.json contents need to be of the form:

```json
{
	"count": 1,
	"git": {
		"label": "develop",
		"searchPaths": "shared,{application}",
		"uri": "<ssh-based-git-repo-url>.git",
		"privateKey" : "<private-key>"
	},
	"encrypt": {
		"key": "<encryption_key>"
	}
}
```

In the example above, "develop" is the name of the branch. Also notice that we are specifying the `searchPaths` to organize the configuration files where application named directories contain configuration specific to applications and `shared` directory containing configurations that apply to all the applications. The order where `shared` appears before the `{application}` matters because config-server will apply the properties in that order (last one wins).

Also, we are providing an [encryption key](http://docs.pivotal.io/spring-cloud-services/1-5/common/config-server/configuring-with-git.html#encryption-and-encrypted-values) so that we can encrypt sensitive information in the properties files.

### Setup bootstrap.yml

For the config-server to locate properties for an app, the app-name needs to be known in the bootstrap phase of app startup in PCF. Add `bootstrap.yml` file to your application under `src/main/resources` with the following content:

```yaml
spring:
  application:
    name: <app-name>
```

### Add maven/gradle dependencies

```
dependencies {
    // other dependencies here ...
    implementation group: 'io.pivotal.spring.cloud', name: 'spring-cloud-services-starter-config-client'
    implementation group: 'org.springframework.boot', name: 'spring-boot-starter-actuator'
}

dependencyManagement {

    // other BOMs here ...

	imports {
		mavenBom "io.pivotal.spring.cloud:spring-cloud-services-dependencies:2.0.1.RELEASE"
	}
}

```

#### Cloud Foundry - Bind the service to the app
To bind the service to the app, we need to mention the config-server service name in the manifest yaml file(s). It might look like the following:

```yaml
applications:
- name: sandbox-app
  buildpack: java_buildpack_offline
  disk_quota: 1G
  instances: 1
  memory: 1G
  routes:
  - route: sandbox-app.cfapps.io
  path: build/libs/sandbox-app.jar
  services:
  - my-config-server
  env:
    SPRING_PROFILES_ACTIVE: dev
```

### Structure of the configuration git repo

Spring boot loads configurations in a very predictable order and it is defined at: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config.

Also, to understand how to name the configuration files that live in the configuration git repository, refer to: http://docs.pivotal.io/spring-cloud-services/1-5/common/config-server/configuration-properties.html

As shown in the example earlier in this document, the following is a directory structure for apps called `sample-app` and `sandbox-app` for "dev", "qa" and "prod" environments. Also, the `shared` directory contains configuration that apply to all applications.

```
/shared/application-prod.yml
/shared/application-dev.yml
/shared/application-qa.yml
/sample-app/application-dev.yml
/sample-app/application-qa.yml
/sample-app/application-prod.yml
/sandbox-app/application-dev.yml
/sandbox-app/application-qa.yml
/sandbox-app/application-prod.yml
```
## Refreshing configuration changes without application restart

In production environment it is a common need to change configuration property or properties and **not** require application restart i.e.
refresh configuration value(s) dynamically. Spring Boot Actuator (/actuator/refresh) supports this out-of-box. However it requires two things:
1. All the beans be annotated with @RefreshScope.
1. A way to trigger a refresh event by invoking actuator refresh endpoint (/actuator/refresh). 

A user (in production it is someone with production access) will have to manually trigger this refresh event say via curl
or an admin web app that calls /actuator/refresh endpoint of the service.
Once the event is triggered, the configurations will be re-fetched from the Config Server.

**NOTE**: 
Please read how [@RefreshScope](https://projects.spring.io/spring-cloud/spring-cloud.html#_refresh_scope) annotation works carefully.
From the Spring Cloud Config Doc:
```text
@RefreshScope works (technically) on an @Configuration class, but it might lead to surprising behaviour: e.g. it does not mean that 
all the @Beans defined in that class are themselves @RefreshScope. Specifically, anything that depends on those beans cannot rely on 
them being updated when a refresh is initiated, unless it is itself in @RefreshScope (in which it will be rebuilt on a refresh and its 
dependencies re-injected, at which point they will be re-initialized from the refreshed @Configuration). 
```

### Cloud-Native Architecture Problem
This works well if you have only one instance of the service. It gets hard if there are multiple instance of the service running. 
Each one of them have to be called individually manually - for which one has to know the URL of each instance of the service. In a cloud native environment like PCF
it is quite hard to figure out URLs for each service. 

### Solution for refreshing configuration for all instances
A solution to the problem is to propagate refresh event to all service instances using a message bus via a pub/sub model. The service instance processing refresh event
publishes that event on a message bus. All instances subscribe to this event and react to the event by refreshing their configuration from the Config Server.

Fortunately Spring Cloud Bus offers this capability. It support use of RabbitMQ and Kafka as message bus for pub/sub model.
This recipe uses RabbitMQ (aka AMQP) as message bus.

With Spring Cloud Bus, the refresh event is triggered by invoking the endpoint `/actuator/bus-refresh` of the service instead of `/actuator/refresh`.

Once a user triggers the refresh event for a service, the Spring Cloud Bus will receive the refresh event. 
Then it will broadcast the refresh event across all the connected clients through the underlying message 

**NOTE:** <span style="color:red">Use of Spring Cloud Bus means all applications using the same config server and same message bus service automatically
subscribe to refresh events and refresh the configuration.</span>. Spring Cloud Bus provides ability to send refresh event only to select of services as well.

#### Add maven/gradle dependencies

```
dependencies {

    // other dependencies here ...
    
    implementation 'org.springframework.boot:spring-boot-starter-cloud-connectors'
    implementation 'org.springframework.boot:spring-boot-starter-amqp'
    implementation 'org.springframework.cloud:spring-cloud-starter-bus-amqp'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:Greenwich.RELEASE"
    }

```

**NOTE**:   
Spring Cloud version prior to `Greenwich.RELEASE` are **not compatible** with Spring Boot `2.1.0.RELEASE` and higher.  

The following runtime error will be thrown, where Spring Cloud Stream (which uses Spring Integration under the hood) is not able to setup subscribers 
(for the queue or topic depending on message bus platform rabbitmq/kafka, for refresh events):

```text
$ curl -X POST http://localhost:9090/bus-refresh/
{   
 "timestamp":"2019-04-03T03:18:35Z","status":500,"error":"Internal Server Error","message":"Dispatcher has no subscribers for channel 
 'sandbox-boot-app-1.springCloudBusOutput'.; nested exception is org.springframework.integration.MessageDispatchingException: Dispatcher has no subscribers,
 failedMessage=GenericMessage [payload=byte[209], headers={contentType=application/json, id=9814c008-5c95-f94f-3a37-ae34605282a2, timestamp=1554261515632}]",
 "path":"/bus-refresh/"    
}

```

See Spring Cloud Bus [Issue#137](https://github.com/spring-cloud/spring-cloud-bus/issues/137).  

#### Add @RefreshScope annotation to beans

```java
@Configuration
public class ApplicationConfig
{
    @Bean
    @RefreshScope
    public MyBean myBean() {
        ...
    } 
    
    @Bean
    @RefreshScope
    public MyFunBean myFunBean() {
            ...
    } 
    
    ...
    ...
}

// To beans like controllers if they have configuration properties
@RefreshScope
@RestController
public class ApplicationController {

    @Value("${app.timeout:unknown}")
    private String timeout;

    ...
}
```

**NOTE:**   
Make sure **all bean classes** that have configuration are annotated with `@RefreshScope` and all @Bean in @Configuration class also have
 @RefreshScope annotation. Otherwise there will be surprising results where parts of configuration are refreshed and parts are not.

That is, bean classes annotated with @Component, @Service, @Repository, @Controller etc. are also annotated with  @RefreshScope.

@RefreshScope on the @Configuration class itself is **not necessary**. As noted above, technically, it works on @Configuration class but it might lead to 
to surprising behavior: e.g. **it does not mean** that all the @Beans defined in that class are themselves @RefreshScope, unless they are marked **explicitly**.  

@RefreshScope on the @Configuration class will only refresh configuration properties if they are present in the @Configuration class.
```java
@RefreshScope
@Configuration
public class ApplicationConfig
{
    @Value("${app.service-name}")
    private String serviceName;
    
    ...
}
```

It is not required if there are no configuration properties in the @Configuration class itself.

#### Enable Spring Cloud Bus Actuator endpoint for refresh

To expose the `/actuator/bus-refresh` endpoint, you need to add following configuration to the application-*.yml file:

```yaml

management:
  endpoints:
    web:
      exposure:
        include: bus-refresh

```

#### Create RabbitMQ service instance in PCF

```
# If creating on-demand service instance
cf create-service p.rabbitmq single-node-3.7 my-message-bus

# Or if creating shared service instance
cf create-service p-rabbitmq standard my-message-bus

```

#### Bind RabbitMQ service instance to the app

It might look like the following:

```yaml
applications:
- name: Sandbox-App
  buildpack: java_buildpack_offline
  disk_quota: 1G
  instances: 1
  memory: 1G
  routes:
  - route: sandbox-app.cfapps.io
  services:
  - my-config-server
  - my-message-bus
  env:
    SPRING_PROFILES_ACTIVE: dev
```
#### Test that the refresh is working
Make changes to configuration yml file(s) and push the changes to git server.

Refresh the configuration by invoking the message bus refresh endpoint for the service:
```text
curl -X POST https://sandbox-app.cfapps.io/actuator/bus-refresh
# or if actuator context path has been moved "/"
```
The above command will result in all application using config server instance and RabbitMQ service instance refreshing the configuration.

Check the modified property has new value. This may require use of a special purpose app to view all properties for an app.

To target specific applications, use the destination parameter:
```text
curl -X POST https://sandbox-app.cfapps.io/actuator/bus-refresh?destination=app1:app2

```
In the above command `app1` and `app2` are `spring.application.name` specified in the `bootstrap.yml` of the PCF application.

**NOTE:** Targeting specific application doesn't work in Pivotal Services Config Server, support for it is on the Pivotal Services roadmap for future delivery. 

#### Customizing Message Broker 
Spring Cloud Bus uses Spring Cloud Stream. Spring Cloud Stream relies on Spring Boot autoconfiguration conventions for configuring 
middleware. For instance, the AMQP broker address can be changed with spring.rabbitmq.* configuration properties however this is not needed
when running on PCF as RabbitMQ connection information is injected by PCF via VCAP_SERVICES. Spring Cloud Bus has 
a handful of native configuration properties in spring.cloud.bus.*, that may be of interest. For example, spring.cloud.bus.destination is the name of 
the topic to use as the external middleware. Normally, the defaults suffice.

Check:   
Spring Cloud Bus [customizing message broker](https://cloud.spring.io/spring-cloud-bus/single/spring-cloud-bus.html#_customizing_the_message_broker).   
Spring Cloud Stream properties in the [Configuration Options](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_configuration_options) section.

## References
- [RefreshScope](https://projects.spring.io/spring-cloud/spring-cloud.html#_refresh_scope) 
- [Configuration CLient & RefreshScope](https://spring.io/guides/gs/centralized-configuration/#_reading_configuration_from_the_config_server_using_the_config_client)
- [Refresh Client Application Configuration](https://docs.pivotal.io/spring-cloud-services/2-0/common/config-server/writing-client-applications.html#refresh-client-application-configuration)
- [Spring Cloud Bus](https://cloud.spring.io/spring-cloud-bus/single/spring-cloud-bus.html)
- [Push Notification and Spring Cloud Bus](https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html#_push_notifications_and_spring_cloud_bus)
- [Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/)
