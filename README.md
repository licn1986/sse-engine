# SSE ENGINE

The SSE ENGINE is a springboot based library that provides a common underlying functionality for server side components to manage client (browser) application subscription instances at runtime and push server originated events on periodic basis depending of application needs/requirements.

## Prerequisites

- Java 11

## SSE Engine Architecture

* [Architecture]

## Getting started
In order to use this library, do the following:

Add following dependency in your application project's maven file (pom.xml):

`---------------------------------------------------------`

`<dependency>`

`   <groupId>com.bally.evolve.core</groupId>`

`   <artifactId>sse-engine</artifactId>`

`   <version>${revision}</version>`

`</dependency>`

`---------------------------------------------------------`

Add following properties in application.properties specified. Values can be adjusted based on application needs/requirements:

`---------------------------------------------------------`

`sse-engine.event-timeout=60000`

`sse-engine.event-publish-fixed-delay=PT2S`

`sse-engine.event-queue-cleanup-fixed-delay=PT30S`

`sse-engine.event-flux-registry-cleanup-fixed-delay=PT60S`

`---------------------------------------------------------`

Make sure to import the correct xxxBeanConfig when using Spring MVC or WebFlux:

`-----------------------------------------------------------------------------------------------------`

`@Import(SseEngineWebMvcBeanConfig or SseEngineWebFluxBeanConfig)`

`-----------------------------------------------------------------------------------------------------`

## Technology Stack

- Spring boot (MVC/WebFlux) v2.6.5
- Lombok
- Mockito
- Junit (Jupiter)
- AssertJ
- Awaitability
- Jackson
