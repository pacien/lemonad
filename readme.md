# lemonad

_Some functional sweetness for Java._

This library provides useful monads that are not present in the JDK.
The defined types and API are minimal and highly composable with the standard ones, avoiding re-inventing them.


## Usage

The Javadoc for the latest snapshot version is available online [here][javadoc].

[javadoc]: https://javadoc.jitpack.io/org/pacien/lemonad/master-SNAPSHOT/javadoc/

### Attempt

The `Attempt` monad represents a computation which can be either successful or failed.
It allows the transformation of results and the recovery of errors in a pipeline,
similarly to [Scala's `Try`][scala-try] or [Java's `CompletableFuture`][java-completable-future].

This monad does __not__ require the error type to be a `Throwable`,
the use of which being problematic in performance-sensitive contexts.

[scala-try]: https://www.scala-lang.org/api/2.12.8/scala/util/Try.html
[java-completable-future]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html

```java
import static org.pacien.lemonad.attempt.Attempt.*;

(tree.hasLemon() ? success(tree.getLemon()) : failure("No lemon."))
  .recoverError(error -> store.buyLemon())
  .transformResult(this::makeLemonade)
  .ifSuccess(this::drink);
```

### Validation

The `Validation` monad represents a validation of a subject which can be successful or failed with errors.
Those errors are aggregated from all the checks that have failed.

```java
import org.pacien.lemonad.validation.Validation;

Validation.of(lemon)
  .validate(not(Lemon::isRotten), "Bad lemon.")
  .validate(Lemon::juiceContent, mL -> mL >= 40, "Not juicy")
  .ifValid(this::makeLemonade)
  .ifInvalid(errors -> makeLifeTakeTheLemonBack());
```


## Setup

_lemonad_ requires Java 11 or above.
Binaries are compiled by and distributed through [JitPack][jitpack-page].

[jitpack-page]: https://jitpack.io/#org.pacien/lemonad

### Gradle (`build.gradle`)

```groovy
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'org.pacien:lemonad:60a6659264'
}
```

### Maven (`pom.xml`)

```xml
<project>
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
 
  <dependency>
    <groupId>org.pacien</groupId>
    <artifactId>lemonad</artifactId>
    <version>60a6659264</version>
  </dependency>
</project>
```


## License

Copyright (C) 2019 Pacien TRAN-GIRARD.

_lemonad_ is distributed under the terms of GNU Affero General Public License v3.0, as detailed in the attached `license.md` file.
