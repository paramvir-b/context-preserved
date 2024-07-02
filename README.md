# Overview

This library helps setting up and retaining [ThreadLocal](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html) across various Java concurrency related classes like Executors, Callables, Runable, etc.

You need to provide your own [ContextCoordinator](lib/src/main/java/com/rokoder/concurrency/contextpreserved/ContextCoordinator.java). You can look at [context-preserved-slf4j](https://github.com/paramvir-b/context-preserved-slf4j) to see how we use it to manage Sl4j MDC across threads.

There is common need to add thread local context and restore it as we make calls across threads and we have to repeat the code. This library provides easy to use apis for such cases.

# Usage

## Build Dependency

Below are some of the common ones, more can be found [here](https://central.sonatype.com/artifact/com.rokoder.concurrency/context-preserved)

### Gradle

Kotlin

```kotlin
implementation("com.rokoder.concurrency:context-preserved:1.0.1")
```

Groovy

```groovy
implementation group: 'com.rokoder.concurrency', name: 'context-preserved', version: '1.0.1'
```

Groovy Short

```groovy
implementation 'com.rokoder.concurrency:context-preserved:1.0.1'
```

### Maven

```xml
<dependency>
    <groupId>com.rokoder.concurrency</groupId>
    <artifactId>context-preserved</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Basic

Below is basic example but you can read more in java doc of `Slf4jMdcPreservedFactory`
