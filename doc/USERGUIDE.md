# User Guide

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

## 1. Example Use-Cases

Before we get into the details, lets have a look at some example use cases. At least to me it is easier to
understand a library from examples, rather than textual description...

### 1.1 Fetching two contents for a single page in parallel

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

### 1.2 Fetching content with a fallback to some static content on error

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

Retries are executed before running into the fallback - and of course, a fallback for a failed retry of a failed request
will be executed one after another...

Be aware of the performance impact of retries and fallbacks, because both will have consequences for your response
times (at least in the higher percentiles: a request that is failing 1% of the time will have a heavy impact on the
99 and higher percentiles, if a retry is combined with a fallback that is secured by a retry and
a fallback...

### 1.3 Fetching the first content that is not empty

Instead of using fallbacks, we can directly configure two ore more other content providers and select the first that
is returning a non-empty and non-error content for a position:

     // Specify what to fetch:
     final Page page = consistsOf(
             fragment(X, withFirst(of(
                     contentFrom(serviceClient, "http://example.com/someContent", "text/html"),
                     contentFrom(serviceClient, "http://example.com/someOtherContent", "text/html"))
             )
     ));

     // Fetch contents of the page:
     final Contents result = page.fetchWith(emptyParameters());

In contrast to the fallback providers, these contents are fetched in parallel. While this will introduce more load, the
performance impact of a failing content is much lower.

The ordering of the contents will be preserved: the first one is more important than the second one, and so on...

If the ordering of these contents is not important, there is another possibility that is simply selecting the content
from the quickest-responding content provider. Using this could really boost your performance, as it is an
implementation of the fan-out-and-quickest-wins pattern.

### 1.4 Fetch contents using the results of an initial call to a microservice

Not recommended, but sometimes required is the next example. Here we are fetching some content and *after that*, we
are extracting information from the response that is needed to access more contents:

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

## 2. Describing Pages

One of the basic ideas of `rx-composer` is to have a relatively simple 'layout' microservice that decides about how a
page is structured, and several other microservices responsible for a single fragment that might be included in other
pages.

Just like `example-composer`, a typical layout microservice will have some template to render HTML. The template
refers to a number of fragments, each having some ID or reference.

In order to fill the fragments with content, `rx-composer` provides an internal DSL to describe how to fetch the
fragments of a page.

### 2.1 Page

A page object is an immutable (and therefore thread-safe and reusable) collection of the page [fragments](#fragment).
You can build a page object using the static factory method `Page.consistsOf(...)`:

```java
    final Page page = consistsOf(someFragments);
```

Now the fragments can be fetched:

```java
    final Contents contents = page.fetchWith(parameters, tracer)
```

The resulting contents can now be rendered and/or processed using the templating library of your choice.

### 2.2 Fragments

Every fragment has a unique `Position` that is refering to the place in the page, where the fragment is positioned.


*TODO*


### 2.3 Position

`Postition` is an interface that is only defining the method `String name()`. Because of this, it is easy to create
a Position using a lambda function: `() -> "MyPosition"` can be used whereever a position is expected.

> The contract of the interface is, that two positions are equal, if their `name()` is equal!

If you need more than a few positions, it es more convenient to use an interface that is implementing `Position`:

```java
    public enum BasicPosition implements Position {
        HEADER, NAVBAR, BODY, FOOTER
    }
```

Because every enum already has a method `name()` using enums is easy.

There is a implementation of the interface, that can be used for testing purposes (or if you do not care about proper
names for your fragments): `AbcPosition` is an enum, that is defining the enum constants A to Z.

### 2.4. Building Fragments

The creation of fragments is supported by the factory class `Fragments`. The following example is showing all the
currently implemented features of the class:

```java
    final ContentProvider fetchInitial =        // ...
    final ContentProvider thenFetch =           // ...

    final Page page = consistsOf(
        fragment(                                   // [1] Fragments.fragment(...)
                X,
                fetchInitial,
                followedBy(                         // [2] Fragments.followedBy(...)
                        (c)-> this::extractParams,
                        fragment(Y, thenFetch)),    // [3] Fragments.fragment(...)
         ),
         fragment(                                  // [4] Fragments.fragment(...)
                Z,
                fetchSomethingElse
         )
    );
```

A page is described, that contains three fragments X, Y and Z. The first fragment is fetched [1] by a
[ContentProvider](#contentprovider) `fetchInitial`.

After this fragment is loaded, fragment Y is fetched [2] using `ContentProvider thenFetch`. The lambda function that
is expected by `followedBy` is used to extract `Parameters` from the previous result of X. These parameters are used
as an input parameter, when Y is fetched [3]. This way, fragments can be requested depending on the response of a
previous fragment.

While fragment Y is depending on X, fragment Z [4] is completely independent. In fact it is requested in parallel to X
and Y, as soon as the page is fetched.

Using these kind of fragments should be sufficient in most situations - but of course it is possible to extend the
Fragments API. Possible extensions could include more sophisticated ways to conditionally include fragments: I am
looking forward to your contribution :-)

### 2.5 ContentProvider

The fragments are primarily placeholers for the content that is loaded by a `ContentProvider`. This interface defines a
single method:

```java
    Observable<Content> getContent(final Position position, final Tracer tracer, final Parameters parameters);
```

The method gets three parameters:

1. *Position*: This is the [position](#fragment) of the fragment where the resulting [content](#content) is placed.
2. *Tracer*: Similar to a logger, a [tracer](#tracer) can be used to gather information about the execution of a page.
Because debugging a multithreaded and/or async program is really difficult, the tracer might be really important,
if fetching content is not working as expected.
3. *Parameters*: key-values used to generate URLs from uri templates. A typical usage of the parameters is to add
request-parameters and configuration parameters when a `Page` is [fetched](#page).

The result of the `getContent()` method is an `Observable<Content>`. Observables are a concept used by reactive
frameworks. It basically means, that `getContent()` is not directly fetching `Content`, but that the fetching is
delayed until some client is subscribing to the result later: in our case, when the page is fetched.

The `rx-composer` library is already containing a number of providers. The `ContentProviders` factory contains a
number of factory methods, to build these providers:

#### 2.5.1 Parameters

#### 2.5.2 Tracer

## 3. Creating ContentProviders

#### 3.1 HttpContentProvider

This is the most important provider. It is accessing remote services using HTTP to GET content using a service-client.
It can be created using the following factory methods.

This method is used to fetch content from a URL, accepting some media type.
The service client is encapsulating the HTTP client used to get the content.
```java
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final String url,
                                              final String accept) {...}
```

Many times, the URL must be constructed from an uri template, using the parameters
of the page request or some application properties:
```java
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final UriTemplate uriTemplate,
                                              final String accept) {...}
```

If fetching the content fails, you might want to try some fallback to get different
contents (from a cached value, a different service or some "default" stuff). In this
case, the ServiceClient is required to be resilient.
```java
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final String url,
                                              final String accept,
                                              final ContentProvider fallback) {...}
```

Again supporting uri templates, this time with a fallback.
``` java
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final UriTemplate uriTemplate,
                                              final String accept,
                                              final ContentProvider fallback)
```

Using these methods in a page configuration is easy and straightforward:

```java
    final ServiceClient cli = singleRetryClient();

    final Page page = consistsOf(
         fragment(X, contentFrom(cli, "http://example.com/someContent", "*/*")),
         fragment(Y, contentFrom(cli, "http://example.com/someOtherContent", "*/*"))
    );
```

The `ContentProviders` API also contains some semantic sugar, that might improve readability of the page config. The
following is equivalent to the above example:

```java
    final ServiceClient cli = singleRetryClient();

    final Page page = consistsOf(
         fragment(
                X,
                withSingle(
                    contentFrom(cli, "http://example.com/someContent", "*/*"))),
         fragment(
                Y,
                withSingle(
                    contentFrom(cli, "http://example.com/someOtherContent", "*/*")))
    );
```

> In addition to these methods, the `ContentProviders` class also provides a number of methods that do accept a
> *fallback provider* in addition to the primary `ContentProvider`. Please have a look at
> section [Resiliency](#resiliency) for more information.

### 3.2 SelectingContentProvider

*TODO*

### 3.3 QuickestWinsContentProvider

*TODO*

### 3.3 Implementing a ContentProvider

*TODO*

(non-blocking would be a good idea;)

### 3.4 Extracting Contents

>
> Available with 1.0.0.M2-SNAPSHOT
>

By default, the ContentProviders simply return the contents received from the microservices. This will only work, if
the called services only return HTML fragments, without any <html> or <body> elements.

In many situations, it will be necessary to extract only parts of the returned HTML: for example, only the <body> part
of a page returned by a microservice.

In order to extract the html body, you can simply wrap a `ContentProvider` by a content mapper:

```java
    fragment(X, withSingle(
            htmlBodyOf(
                    contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_HTML)
            )
    ))
```

The static utility function `htmlBodyOf` is implemented by `ContentMappers`. Implementing your own mapper is easy: just
implement some static method like this:

```java
    public static ContentProvider myContentMapper(final ContentProvider contentProvider) {
        return (position, tracer, parameters) -> {
            return contentProvider
                    .getContent(position, tracer, parameters)
                    .map((Content content) -> ... );
        };
    }
```

## 4. Fetching Contents

### 4.1 Content

*TODO*

#### 4.1.1 ErrorContent

#### 4.1.2 StaticTextContent

#### 4.1.3 HttpContent

### 4.2 Contents

## 5. Using ServiceClients

*TODO*

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

## 6. Resiliency

*TODO*

### 6.1 Timeouts

*TODO*

### 6.2 Retries

*TODO*

### 6.3 Fallbacks

A fallback is an alternative `ContentProvider` that is used, if some service is responding with an HTTP server or
client error, or if an exception is thrown by the `ContentProvider`.

> **Caution:** Please note, that a fallback is executed *after* some error occured. This will lead to longer response
  times for your page, especially if combined with retries, of if the fallback is using retries and/or fallbacks
  itself!

#### 6.3.1 ContentProviders with Fallbacks

The fallbacks expected by the above methods can be all kind of ContentProviders: not only providers accessing other
microservices, but also others, that simply return static content or some cached entries from the last successful
request. Again, we have some semantic sugar to improve readability:

```java
    final ServiceClient cli = singleRetryClient();

    final Page page = consistsOf(
         fragment(
                X,
                withSingle(
                    contentFrom(cli, "http://example.com/someContent", "*/*",
                    fallbackTo(
                        contentFrom(cli, "http://example.com/someOtherContent", "*/*")
                    )
                ))),
         fragment(Y,
                ...
    );
```

Instead of using a ContentProvider as a fallback, you can also fall back to an `Observable<Content>` or directly
to a `Content` instance:

```java
    ...
        fallbackTo(
            fetchFromMemcached("some-fallback-key")
        )
    ...

    public Observable<Content> fetchFromMemcached(final String cacheKey) {
        ...
    )
```

or using some existing Content instance:

```java
        fallbackTo(
            staticTextContent(              // class StaticTextContent implements Content
                "static error message",
                position,
                "<strong>Sorry, something went wrong...</strong>"
            )
        )
```

#### 6.3.2 Using SelectingContentProvider for Fallbacks

*TODO*
ContentProviders.withFirst() is selecting the first non-empty `Content`. Basically, this is a different way to
use fallbacks... But: while fallbacks are executed sequentially, the SelectingContentProvider is fetching content in
parallel. The advantage is obviously better response time in case of a fallback - but the downsides are a more complex
Page configuration and much more load for the microservices. Advice: use fallbacks, if things should go ok most of the
time; use SelectingContentProvider if the primary service is regularly unable to provide the requested content.
*/TODO*

### 6.4 CircuitBreaker

*TODO*
Fail-Fast
Fail-Fast only for exceptions and server errors (HTTP 5xx). No Circuit-Breaking for HTTP 400! Prefer
Contents.withFirst() over fallbacks
*/TODO*

### 6.5 ServiceClients and CircuitBreakers

*TODO*
There is a one-to-one relationship between a ServiceClient and a Hystrix CircuitBreaker: if the Circuit is OPEN,
*all* requests using the service client are affected! This is, because the Ref of the client is used as a key for
the Hystrix command...
*/TODO*

