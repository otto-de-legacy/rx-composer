# RX-Composer

A DSL for reactive composition of content from different microservices using RxJava.

## Status

Far from beeing stable. More of an early proof of concept.

## About

Microservice architectures for web applications are currently at the top of the hype cycle. One of the most interesting (and complicated)
questions is how to integrate microservices in the frontend. The following options are already well understood:
* Different pages are rendered by different microservices (or self-contained systems). Hyperlinks are used to "integrate" the different pages.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated using AJAX and similar techniques.
* A page is rendered by some microservices: one service is rendering the initial HTML, others are integrated using Edge-Side includes (or Server-Side Indludes) using reverse-proxies.
* Services are calling other services in order to get data or HTML fragments for different parts of the page. The Frontend-Service is integrating the results from the different services into a single HTML page.

Rx-Composer is meant to solve some problems of the latter solution: 

## Features

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
