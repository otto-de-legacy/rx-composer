# RX-Composer

A DSL for reactive composition of content from different microservices using RxJava.

## Status

[![Next Selected Stories](https://badge.waffle.io/otto-de/rx-composer.svg?label=Ready&title=Next)](http://waffle.io/otto-de/rx-composer)
[![Active Stories](https://badge.waffle.io/otto-de/rx-composer.svg?label=In%20Progress&title=Doing)](http://waffle.io/otto-de/rx-composer)

[![build](https://travis-ci.org/otto-de/rx-composer.svg?branch=master)](https://travis-ci.org/otto-de/rx-composer?branch=master)
[![codecov](https://codecov.io/gh/otto-de/rx-composer/branch/master/graph/badge.svg)](https://codecov.io/gh/otto-de/rx-composer)
[![release](https://maven-badges.herokuapp.com/maven-central/de.otto.rx-composer/composer-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.rx-composer/composer-core)
[![license](https://img.shields.io/github/license/otto-de/rx-composer.svg)](./LICENSE)

_**Work in Progress**_

* Ready to be tested
* Not yet performance- and/or load-tested
* Not yet in production at otto.de
* Partly documented

## About

Microservice architectures for web applications are currently at the top of the hype cycle. One of the most
interesting (and complicated) questions is how to integrate microservices in the frontend. The following options
are already well understood:

* Different pages are rendered by different microservices (or self-contained systems). Hyperlinks are used to
"integrate" the different pages.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated
using AJAX and similar techniques.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated
using Edge-Side includes (or Server-Side Indludes) using reverse-proxies.
* Services are calling other services in order to get data or HTML fragments for different parts of the page.
The Frontend-Service is integrating the results from the different services into a single HTML page.

The latter solution requires a frontend microservice to gather content from multiple backend microservices.
The more fine-grained you are cutting your microservices, the more important it gets to use asynchronous
communication and retrieve content in parallel, whenever possible. Especially in Java, implementing this in
a tradition way is a non-trivial task.

In addition to this, you would have to deal with unavailable backend services, slow service, and so on: you have
to implement resiliency against failures into your frontend service(s). This is also a non-trivial task...

Rx-Composer is meant to solve such kind of problems. It provides you with an easy to read DSL to describe, what
content to fetch from which microservices. It is handling failures when retrieving content, and it retrieves
content in a reactive way, using RxJava in the implementation.

## Examples

### Fetching two contents for a single page in parallel

The general pattern for a rx-composer server is to describe the contents to be fetched, then execute the page and
process the resulting contents.

In the first example, we want to fetch two HTTP resources for fragements "X" and "Y":

     // Setup some ServiceClient:
     final ServiceClient serviceClient = HttpServiceClient.singleRetryClient();

     // Specify what to fetch:
     final Page page = consistsOf(
             fragment(
                     X,
                     withSingle(
                            contentFrom(serviceClient, "http://example.com/someContent", "text/html")
                     )
             ),
             fragment(
                     Y,
                     withSingle(
                            contentFrom(serviceClient, "http://example.com/someOtherContent", "text/html")
                     )
             )
     );

     // Fetch contents of the page:
     final Contents result = page.fetchWith(emptyParameters());

     // now we can process the results:
     System.out.println("X: " + result.getBody(X);
     System.out.println("Y: " + result.getBody(Y);

Both contents will be requested in parallel. The time to fetch both contents will only be a little slower than the
slowest response - instead of the sum of both response times.

Because we are using the `singleRetryClient`, we already have some build-in resiliency:
* The client is is using some default timeouts (for example, it is using a read-timeout of 500ms).
* A Hystrix Circuit-Breaker is used, so using the client will fail-fast, if 50% of all requests are failing.
* If fetching the resource fails because of a timeout, exception or HTTP server error, the request is
retried once.

### Fetching content with a fallback to some static content on error

 Using a ServiceClient like `noRetries()` or `singleRetry`, it is possible to configure a fallback, if retrieving
 content from the primary ContentProvider fails because of timeouts, exceptions or HTTP server errors.

 ```java
     final Page page = consistsOf(
             fragment(X,
                     withSingle(
                            contentFrom(serviceClient, "http://example.com/someContent", TEXT_PLAIN,
                                    fallbackTo(staticTextContent(position, "<p>Some Fallback Content</p>"))
                            )
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

### More Examples

...about how to use `rx-composer` can be found in the [user guide](./doc/USERGUIDE.md).

## Features

_TODO_

### Server-Side Transclusion

_TODO_

### Asynchronous Execution

_TODO_

### Build-In Resiliency

_TODO_

### Tracing and Debugging

_TODO_

## Documentation

The `docs` folder contains some documentation about the project:

1. **Change Log:** Information about the lasted changes, release notes, and so on can be found in the
[change log](CHANGELOG.md).

2. **Getting Started:** The [Getting Started](./doc/GETTINGSTARTED.md) guide contains information about how to include
rx-composer into your project, about build it, and so on.

3. **Examples:** A working example is also contained in this project. It consists of two Spring Boot server modules and
is described [here](./doc/EXAMPLES.md).

4. **User Guide:** If you are looking for some deeper insights, the [User Guide](./doc/USERGUIDE.md) might be the
right place to look.

5. **Contributing:** You would like to contribute? Great! Please have a look [here](./doc/CONTRIBUTING.md) for
information about how to do it.

