# RX-Composer

A DSL for reactive composition of content from different microservices using RxJava.

## Status

_**Work in Progress**_

## About

Microservice architectures for web applications are currently at the top of the hype cycle. One of the most interesting (and complicated)
questions is how to integrate microservices in the frontend. The following options are already well understood:
* Different pages are rendered by different microservices (or self-contained systems). Hyperlinks are used to "integrate" the different pages.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated using AJAX and similar techniques.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated using Edge-Side includes (or Server-Side Indludes) using reverse-proxies.
* Services are calling other services in order to get data or HTML fragments for different parts of the page. The Frontend-Service is integrating the results from the different services into a single HTML page.

The latter solution requires a frontend microservice to gather content from multiple backend microservices. The more fine-grained you are cutting your microservices, the more important it gets to use asynchronous communication and retrieve content in parallel, whenever possible. Especially in Java, implementing this in a tradition way is a non-trivial task.

In addition to this, you would have to deal with unavailable backend services, slow service, and so on: you have to implement resiliency against failures into your frontend service(s). This is also a non-trivial task...

Rx-Composer is meant to solve such kind of problems. It provides you with an easy to read DSL to describe, what content to fetch from which microservices. It is handling failures when retrieving content, and it retrieves content in a reactive way, using RxJava in the implementation.

## Examples

### Using ServiceClient configurations

In order to retrieve content from other mircoservices, ServiceClients are used. There are some predefined
ServiceClients like:
* `HttpServiceClient.singleRetryClient():` A client that is using a circuit breaker with single retry to access content.
* `HttpServiceClient.noRetriesClient():` Another client, that is not doing retries, but still using a circuit breaker.
* `HttpServiceClient.noResiliencyClient():` A client without any circuit breaker or retries, but with the possibility to
 configure timeouts.

```java
        try (final ServiceClient serviceClient = singleRetryClient()) {
            final Page page = ...
        );

```

By default, all clients are configured with a connectTimeout of 1000ms and a readTimeout of 500 ms. It is possible to override these
defaults like this:

```java
        enum CustomRef implements Ref {exampleConfig}

        ...

        try (final ServiceClient serviceClient = singleRetryClient(exampleConfig, 200, 50)) {
            final Page page = ...
        );

```

### Fetching two contents for a single page in parallel:
       
            // Specify what to fetch:
            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(serviceClient, "http://example.com/someContent", "text/html"))
                    ),
                    fragment(
                            Y,
                            withSingle(contentFrom(serviceClient, "http://example.com/someOtherContent", "text/html"))
                    )
            );
            // Fetch contents of the page:
            final Contents result = page.fetchWith(emptyParameters());

### Fetching content with a fallback to some static content on error:

Using a ServiceClient like `noRetries()` or `singleRetry`, it is possible to configure a fallback, if retrieving content
from the primary ContentProvider fails because of timeouts, exceptions or HTTP server errors:

```java
            final Page page = consistsOf(
                    fragment(X,
                            withSingle(contentFrom(serviceClient, "http://example.com/someContent", TEXT_PLAIN,
                                    fallbackTo((position, parameters) -> just(fallbackContent(position, "Some Fallback Content"))))
                            )
                    )
            );

```

The `fallbackTo` methods accepts all kinds of ContentProviders. The following example is falling back to a different
service, using a differently configured ServiceClient:

```java
            ...
                    contentFrom(serviceClient, "http://example.com/someContent", TEXT_PLAIN,
                            fallbackTo(contentFrom(fallbackClient, "http://example.com/someFallbackContent", TEXT_PLAIN))
                    )
            ...

```

### Fetching the first content that is not empty:

            // Specify what to fetch:
            final Page page = consistsOf(
                    fragment(X, withFirst(of(
                            contentFrom(serviceClient, "http://example.com/someContent", "text/html"),
                            contentFrom(serviceClient, "http://example.com/someOtherContent", "text/html"))
                    )
            ));

            // Fetch contents of the page:
            final Contents result = page.fetchWith(emptyParameters());

### Fetch contents using the results of an initial call to a microservice:

            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(serviceClient, "http://example.com/someContent", "text/plain")),
                            followedBy(
                                    (final Content content) -> parameters(of("param", content.getBody())),
                                    fragment(
                                            Y,
                                            withSingle(contentFrom(serviceClient, fromTemplate("http://example.com/someOtherContent{?param}"), TEXT_PLAIN))
                                    ),
                                    fragment(
                                            Z,
                                            withSingle(contentFrom(serviceClient, fromTemplate("http://example.com/someDifferentContent{?param}"), TEXT_PLAIN))
                                    )
                            )
                    )
            );
            
## Features

Pending...

## Building RX-Composer

If you want to build RX-Composer using Gradle, you might want to use
 the included Gradle wrapper:

```
    bin/go build
```
 or

```
    bin/gradlew build
```

 An IntelliJ IDEA Workspace can be created using

```
    bin/go idea
```

If you do not want to use the provided gradle wrapper, please make sure
that you are using an up-to-date version of Gradle (>= 2.12.0).

## Version History

### Current Snapshot

* Initial Release
