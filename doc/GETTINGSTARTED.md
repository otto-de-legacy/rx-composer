# Getting Started

## Gradle installation

The latest release of the `rx-composer` libraries can be found on Maven Central and
the Sonatype release repository:

```
 repositories {
     mavenCentral()
     maven { url 'https://oss.sonatype.org/content/repositories/releases/' }
 }

```

If you want to use the latest SNAPSHOT, you can find them in the Sonatype snapshot repo:

```
 repositories {
     maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
 }
```

Currently, `rx-composer` is consisting of two libraries:
* `composer-core`: The core lib of this project.
* `composer-thymeleaf`: An optional Thymeleaf dialect used to make it easier to integrate
 `rx-composer` into your Thymeleaf templates.

In order to use these libraries, you have to add them as a compile time dependency to
 your build.gradle:

```
 dependencies {
     compile("de.otto.rx-composer:composer-core:1.0.0.M1-SNAPSHOT")
 }
```

or

```
 dependencies {
     compile("de.otto.rx-composer:composer-core:1.0.0.M1-SNAPSHOT")
     compile("de.otto.rx-composer:composer-thymeleaf:1.0.0.M1-SNAPSHOT")
 }
```


## Building from Source

To build the project from source, you need Java SDK v1.8 or higher.
If you want to build RX-Composer using Gradle, you might want to use the included Gradle wrapper:

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

Also see [CONTRIBUTING.md](CONTRIBUTING.md if you wish to submit pull requests.
