# API Service - the OAuth2 Resource Server
The resource server hosts the [HTTP resources](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Identifying_resources_on_the_Web) 
in which can be a document a photo or something else, in our case it will be a REST API protected by OAuth2.

## Defining our protected API

The code bellow defines the endpoint `/me` which returns the `Principal` object and it requires the authenticated 
user to have the `ROLE_USER` to access. 

[API Controller]()
The `@PreAuthorize` annotation validates whether the user has the given role prior to execute the code, to make it work
it's necessary to enable the `prePost` annotations, to do so add the following class:

The important part here is the `@EnableGlobalMethodSecurity(prePostEnabled = true)` annotation, the `prePostEnabled` flag
is set to `false` by default.

## Resource Server Configuration

To decode the `JWT` token it will be necessary to use the `public key` from the self-signed certificated used on the

Use the following command to export the `public key` from the generated JKS: 

````bash
$ keytool -list -rfc --keystore keystore.jks | openssl x509 -inform pem -pubkey -noout
````

A sample response look like this:

```bash
-----BEGIN PUBLIC KEY-----
-----END PUBLIC KEY-----
```

Copy it to a `public.txt` file and place it at `/src/main/resources` and then configure your `application.yml` pointing
to this file:

```yaml
security:
  jwt:
    public-key: classpath:public.txt
```

Spring configuration for the resource server.


The important part of this configuration are the three `@Bean`s: `JwtAccessTokenConverter`, `TokenStore` and `DefaultTokenServices`:
  - The `JwtAccessTokenConverter` uses the JKS `public key`.
  - The `JwtTokenStore` uses the `JwtAccessTokenConverter` to read the tokens.
  - The `DefaultTokenServices` uses the `JwtTokenStore` to persist the tokens.
