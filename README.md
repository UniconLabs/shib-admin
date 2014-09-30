# Shibboleth IDP relying party config Initializr

A web app to initialize `relying-party.xml` configuration file with user provided config values via its web UI

## This project is WORK IN PROGRESS and has not fully realized the target vision for such config UI

## Running the app locally

This is a [Spring Boot](http://projects.spring.io/spring-boot/) application. The simplest way to run it with an embedded Tomcat server is via Gradle plugin on the command line:

```bash
$ cd shib-idp-relyingparty-initializr
$ ./gradlew bootRun
```

In a web browser access `http://localhost:8080`
