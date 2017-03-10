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

## The Examples

The project contains an example consisting of two Spring Boot applications that can be found in the examples folder:

* examples/example-composer: This module contains a server application, that is using rx-composer to render a
 page that is consisting of a number of fragments. The fragments are served by the second module:
* examples/example-fragments: Here you can find another server that has a few HTTP endpoints, serving some simple HTML
fragments.

Lets first see, what this example is doing and start the example-composer:

```
gradle examples:example-composer:bootRun
```

When the server has started, open `http://localhost:8080` in your browser. You will now see a page,
that is telling you to start the fragment server:


![Composer w/o fragments](images/example-composer-1.png)

Now we start the example-fragments server and reload the page:

```
gradle examples:example-fragments:bootRun
```

After reloading, you should see the same page, but now containing some contents in the different panels.

![Composer w/o fragments](images/example-composer-2.png)

So what happened? The `ComposerServer` has rendered a page using a Thymeleaf template (`content.html`). The
template contains some fragments served by the `FragmentServer`. In the first step, these fragments
where not available, because the server was not yet running. Only the 'Introduction' block and the
last panel contained some text: These are examples on how to specify fallbacks, if an included fragment
is not available. We will come back to this topic when we are talking about [resiliency](#resiliency) and
[fallbacks](#fallbacks).

After the `FragmentServer` started, the included (or [transcluded](https://www.mediawiki.org/wiki/Transclusion)
fragments where rendered.

> Please note, that sometimes you have to reload the page twice to get all contents. For some reason, the very
first request to a service is taking too long, so some requests are timing out. I'm working on it :-)

The `example-composer` consists of three classes:
1. The `ComposerServer`, which is a standard Spring Boot Application. You can start this class in your
  IDE to start up the server.
2. The `PageConfiguration`, which is a Spring Boot `@Configuration` class. This class is configuring two things:
    * The `Page` that describes which (and how) fragments are fetched.
    * The `ServiceClients` used to fetch HTTP content for the different fragments.
3. Finally, the `ContentController`, which is a simple Spring controller that is using the `Page` object to
fetch the contents.

**ContentController**

Let's start with the controller:

```java
    final Contents contents = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
```

The configured `page` is used to `fetch` contents. After executing this, the `Contents` can
be used inside a Thymeleaf template (or whatever templating engine you might prefer) to put
the fragements of the page whereever you like.

The easiest way to include a fragment into a Thymeleaf template is to use the `rxc:fragment` element:

```xml
    <rxc:fragment position="INTRO">
        <!-- here you could add code that is rendered, if the INTRO fragment is unavailable -->
    </rxc:fragment>
```


**PageConfiguration**

The Page object that is used by the controller is coming from `PageConfiguration`. It basically defines the structure
of the fragments and their sources. The INTRO fragement, for example, is configured like this:

```java
    Page.consistsOf(
            fragment(INTRO, withSingle(
                    contentFrom(clients.getBy(introService), "http://localhost:8081/intro", TEXT_HTML))
            ),
            ...

```

In order to get the content from the `/intro` service, a `ServiceClient` is used. These clients primarily refer
to an HTTP client. In order to use differently configured clients, the `PageConfiguration` also configures a number of
`ServiceClients`:

```java
    clients = serviceClients(
                singleRetry(introService, 5000, 1000),
                singleRetry(helloService, 5000, 500),
                noResiliency(somethingElseService, 5000, 400),
                singleRetry(serviceC, 5000, 1000),
                singleRetry(someMissingService, 5000, 480),
                noRetries(someBrokenService, 5000, 550)
        );
```

Every client may use different timeouts and/or other resiliency patterns like circuit-breakers, retries or fallbacks.

## The Page DSL

One of the basic ideas of `rx-composer` is to have a relatively simple 'layout' microservice that decides about how a
page is structured, and several other microservices responsible for a single fragment that might be included in other
pages.

Just like `example-composer`, a typical layout microservice will have some template to render HTML. The template
refers to a number of fragments, each having some ID or reference.

In order to fill the fragments with content, `rx-composer` provides an internal DSL to describe how to fetch the
fragments of a page.

### Page

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

### Fragment

Every fragment has a unique `Position` that is refering to the place in the page, where the fragment is positioned.

**Position**

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

**Fragments**

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

### ContentProvider

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

**HttpContentProvider**

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

**Fallbacks**

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

### Content

*TODO*

### Parameters

*TODO*

### Tracer

*TODO*

## ServiceClient

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

## Resiliency

*TODO*

### Fallbacks

*TODO*

## Example Use-Cases

**Fetching two contents for a single page in parallel:**

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

 **Fetching content with a fallback to some static content on error:**

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

 **Fetching the first content that is not empty:**

     // Specify what to fetch:
     final Page page = consistsOf(
             fragment(X, withFirst(of(
                     contentFrom(serviceClient, "http://example.com/someContent", "text/html"),
                     contentFrom(serviceClient, "http://example.com/someOtherContent", "text/html"))
             )
     ));

     // Fetch contents of the page:
     final Contents result = page.fetchWith(emptyParameters());

 **Fetch contents using the results of an initial call to a microservice:**

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


