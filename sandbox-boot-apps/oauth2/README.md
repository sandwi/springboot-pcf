# Spring Security OAuth2 Authorization and Spring Boot 2


> To learn about OAuth2, read [OAuth2](https://oauth.net/2/) or Okta Sponsored [OAuth](https://www.oauth.com/) site.
> Good set of diagrams: [Diagrams And Movies Of All The OAuth 2.0 Flows](https://medium.com/@darutk/diagrams-and-movies-of-all-the-oauth-2-0-flows-194f3c3ade85)

## Pre-req
 
 - [JDK 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
 - Text editor or your favorite IDE such as Intellij IDEA or Eclipse STS
 - [Gradle 4.10+](https://gradle.org/)

## OAuth2 Terminology

  - **Resource Owner**
    - The user who authorizes an application to access his account. The access is limited to the `scope`.
  - **Resource Server**:
    -  A server that handles authenticated requests after the `client` has obtained an `access token`.
  - **Client**
    - An application that access protected resources on behalf of the resource owner.
  - **Authorization Server**
    - A server which issues access tokens after successfully authenticating a `client` and `resource owner`, and authorizing the request.
  - **Access Token**
    - A unique token used to access protected resources
  - **Scope**
    - A Permission
  - **JWT**
    - JSON Web Token is a method for representing claims securely between two parties as defined in [RFC 7519](https://tools.ietf.org/html/rfc7519)
  - **Grant type**
    - A `grant` is a method of acquiring an access token. 
    - [Read more about grant types here](https://oauth.net/2/grant-types/)

## Implementation Overview
This project uses [Spring Security 5](https://spring.io/projects/spring-security), [Spring Security OAuth2]() and [Spring Boot 2](https://spring.io/projects/spring-security).

This project has two sub-projects:
* oauth2-auth-server: Spring Security OAuth2 Authorization Server
* sandbox-user-service: An API server i.e an OAuth2 Resource Server

### Authorization Server

The OAuth2 `Authorization Server` is built using [Spring Security 5.x](https://spring.io/projects/spring-security) and [Spring Boot 2.0.x](https://spring.io/projects/spring-boot).

#### Dependencies
See [build.gradle](https://github.com/sandwi/springboot-pcf/blob/master/sandbox-boot-apps/oauth2/build.gradle).
The dependencies can be generated from Spring Initializer at [start.spring.io](https://start.spring.io/).

#### Database
In this app, [H2 Database](http://www.h2database.com/html/main.html) is used as database for Spring OAuth2 Authorization Server related tables. OAuth2 SQL schema required by Spring Security OAuth2 are:

```sql
CREATE TABLE IF NOT EXISTS oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256) NOT NULL,
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4000),
  autoapprove VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_client_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name VARCHAR(256),
  client_id VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_access_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
);

CREATE TABLE IF NOT EXISTS oauth_code (
  code VARCHAR(256), authentication BLOB
);
```

Seed data:   

```sql
-- The encrypted client_secret it `secret`
INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, authorities, access_token_validity)
  VALUES ('clientId', '{bcrypt}$2a$10$vCXMWCn7fDZWOcLnIEhmK.74dvK1Eh8ae2WrWlhr2ETPLoxQctN4.', 'read,write', 'password,refresh_token,client_credentials', 'ROLE_CLIENT', 300);
```

* The `client_secret` above was generated using [bcrypt](https://en.wikipedia.org/wiki/Bcrypt).  
* The prefix `{bcrypt}` is required by Spring Security 5.x's new feature of [DelegatingPasswordEncoder](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#pe-dpe).

Spring Security's default `User` and `Authority` reference SQL schema:

`org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl`.

```sql
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(256) NOT NULL,
  password VARCHAR(256) NOT NULL,
  enabled TINYINT(1),
  UNIQUE KEY unique_username(username)
);

CREATE TABLE IF NOT EXISTS authorities (
  username VARCHAR(256) NOT NULL,
  authority VARCHAR(256) NOT NULL,
  PRIMARY KEY(username, authority)
);
```
  
Seed data for the user and authorities:  
   
```sql
-- The encrypted password is `pass`
INSERT INTO users (id, username, password, enabled) VALUES (1, 'user', '{bcrypt}$2a$10$cyf5NfobcruKQ8XGjUJkEegr9ZWFqaea6vjpXWEaSqTa2xL9wjgQC', 1);
INSERT INTO authorities (username, authority) VALUES ('user', 'ROLE_USER');
```
  
#### Spring Security Configuration
Quoting from [Spring Blog](https://spring.io/blog/2013/07/03/spring-security-java-config-preview-web-security#websecurityconfigureradapter):

>The @EnableWebSecurity annotation and WebSecurityConfigurerAdapter work together to provide web based security.

With Spring Boot the `DataSource` object will be auto-configured and you can just inject it to the class instead of defining it yourself. It needs to be injected to the `UserDetailsService` in which will be using the provided `JdbcDaoImpl` provided by Spring Security (if necessary this can be customized this with your own implementation).

As the Spring Security's `AuthenticationManager` is required by some auto-configured Spring `@Bean`s it's necessary to
override the `authenticationManagerBean` method and annotate is as a `@Bean`.

The `PasswordEncoder` will be handled by `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, it handles password encoders and delegates based on a prefix, in our example we are prefixing the passwords with `{bcrypt}`.

### Authorization Server Configuration
The authorization server validates the `client` and `user` credentials and provides the tokens as `JSON Web Tokens` a.k.a `JWT`.

To sign the generated `JWT` tokens we'll be using a self-signed certificate. 

[Spring Security Authorization Server configuration class](https://github.com/sandwi/springboot-pcf/blob/master/sandbox-boot-apps/oauth2/oauth2-auth-server/src/main/java/sandbox/security/oauth2/config/security/AuthorizationServerConfiguration.java).

The above class has all the required Spring `@Bean`s for `JWT`. 
The most important `@Bean`s are: `JwtAccessTokenConverter`, `JwtTokenStore` and the `DefaultTokenServices`.
* The `JwtAccessTokenConverter` uses the self-signed certificate to sign the generated tokens.  
* The `JwtTokenStore` implementation that just reads data from the tokens themselves. Not really a store since it 
never persists anything and it uses the `JwtAccessTokenConverter` to generate and read the tokens.  
* The `DefaultTokenServices` uses the `TokenStore` to persist the tokens.

> Guide [to generate a self-signed certificate](https://dzone.com/articles/creating-self-signed-certificate).

After generating self-signed certificate configure it the `application.yml`.

```yaml
security:
  jwt:
    key-store: classpath:keystore.jks
    key-store-password: letmein
    key-pair-alias: mytestkey
    key-pair-password: changeme
```

### Resource Server Configuration
[API Server OAuth2 Resource Server Configuration](https://github.com/sandwi/springboot-pcf/blob/master/sandbox-boot-apps/oauth2/sandbox-user-service/src/main/java/sandbox/user/service/api/config/ResourceServerConfiguration.java).

### Testing 
* Start OAuth2 Authorization Server: 
```bash
cd oauth2-auth-server
java -jar build/libs/oauth2-oauth-server-0.0.1-SNAPSHOT.jar
```
This will start Auth Server on port 9000.

* Start API Server (Resource Server):
```bash
cd sandbox-user-service
java -jar build/libs/sandbox-user-service-0.0.1-SNAPSHOT.jar
```
This will start Auth Server on port 9100.

#### Generating the token
The command below authenticates the user and obtains an OAuth2 `access_token`

```bash
$ curl -u clientId:secret -X POST localhost:9000/oauth/token\?grant_type=password\&username=user\&password=pass
```

#### Accessing the resource

Use the `access_token` to invoke the API by provides `access_token` as Bearer token in the `Authorization` HTTP Header: 

```bash
curl localhost:9100/me -H "Authorization: Bearer <Insert JWT from previous command>""

```
 
# References
 - [OAuth 2.0](https://oauth.net/2/) 
 - [Spring Boot 2](https://spring.io/projects/spring-boot)
 - [Spring - OAuth2 Developers Guide](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)
 - [Spring Boot 2 and OAuth2 Tutorial](https://spring.io/guides/tutorials/spring-boot-oauth2/)
