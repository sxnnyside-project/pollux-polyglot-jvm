# Pollux Polyglot JVM

![Version](https://img.shields.io/badge/version-0.1.0-blue)
![License](https://img.shields.io/badge/License-MIT-green)
[![CI](https://github.com/sxnnyside-project/pollux-polyglot-jvm/workflows/CI/badge.svg)](https://github.com/sxnnyside-project/pollux-polyglot-jvm/actions)

<p align="center">
  <strong>Universal JVM ✦ Panama FFM ✦ Deterministic Authority</strong><br>
  <em>Deterministic execution authority and sandboxing SDK for the Java Virtual Machine.</em>
</p>

<p align="center">
  <a href="#about">About</a> ✦
  <a href="#features">Features</a> ✦
  <a href="#installation">Installation</a> ✦
  <a href="#usage">Usage</a> ✦
  <a href="#architecture">Architecture</a> ✦
  <a href="#contributing">Contributing</a>
</p>

---

## About

**Pollux Polyglot JVM** is the official Java Virtual Machine (JVM) binding for the Pollux Core deterministic authority sandboxing engine.

It bridges JVM applications (Java, Kotlin, Scala, Groovy) directly with the native Rust Core (`pollux-abi/1`) via OpenJDK Project Panama (Foreign Function & Memory API, JEP 454), eliminating legacy JNI boilerplate, manual wrappers, and memory overhead.

Engine handles verify incoming capability requests against fine-grained Authority Manifests off-heap, returning deterministic evaluation traces and verdict decisions.

### Philosophy

> *"Deterministic capability enforcement with zero JNI overhead and guaranteed off-heap safety."*

This is a Sxnnyside project, part of the Sxnnyside Project's core ecosystem.

## Features

- **Project Panama FFM (JEP 454)**: Direct native downcalls and off-heap memory segments without legacy JNI stubs or C wrappers.
- **Universal JVM Ergonomics**: Full idiomatic interoperability between Java (Java 22+/23+) and JetBrains Kotlin.
- **Leak-Free Resource Safety**: Engine handles implement `AutoCloseable` (`try-with-resources` in Java, `.use { }` in Kotlin).
- **Deterministic Sandboxing**: Evaluates filesystem, network, process execution, and secret capabilities against declared manifests.
- **Strict Typing & Records**: Immutable Java records for `Operation` candidates and `EvaluationResult` verdicts.
- **ABI Version Verification**: Instantly validates `pollux-abi/1` compatibility upon engine instantiation.

## Installation

### Prerequisites

- Java Development Kit (JDK 22 or 23+)
- JVM argument: `--enable-native-access=ALL-UNNAMED`

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.sxnnysideproject:pollux-polyglot-jvm:0.1.0")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'com.sxnnysideproject:pollux-polyglot-jvm:0.1.0'
}

tasks.withType(JavaExec) {
    jvmArgs '--enable-native-access=ALL-UNNAMED'
}
```

### Maven

```xml
<dependency>
    <groupId>com.sxnnysideproject</groupId>
    <artifactId>pollux-polyglot-jvm</artifactId>
    <version>0.1.0</version>
</dependency>
```

### From Source

```bash
git clone https://github.com/sxnnyside-project/pollux-polyglot-jvm.git
cd pollux-polyglot-jvm

just install
just check
```

## Usage

### Java (try-with-resources)

```java
import com.sxnnysideproject.pollux.PolluxEngine;
import com.sxnnysideproject.pollux.Operation;
import com.sxnnysideproject.pollux.EvaluationResult;

public class Application {
    public static void main(String[] args) {
        String manifestYaml = """
            version: 1
            filesystem:
              read:
                - ./config
                - ./assets
            """;

        try (PolluxEngine engine = PolluxEngine.load(manifestYaml)) {
            // 1. Evaluate allowed read
            EvaluationResult readResult = engine.evaluate(Operation.fileRead("./assets"));
            System.out.println("Can read ./assets: " + readResult.allowed()); // true

            // 2. Evaluate unauthorized write
            EvaluationResult writeResult = engine.evaluate(Operation.fileWrite("./assets"));
            System.out.println("Can write ./assets: " + writeResult.allowed()); // false
            System.out.println("Outcome: " + writeResult.outcome()); // "deny"
        }
    }
}
```

### Kotlin

```kotlin
import com.sxnnysideproject.pollux.PolluxEngine
import com.sxnnysideproject.pollux.Operation

fun main() {
    val manifestYaml = """
        version: 1
        filesystem:
          read:
            - ./assets
    """.trimIndent()

    PolluxEngine.load(manifestYaml).use { engine ->
        val result = engine.evaluate(Operation.fileRead("./assets"))
        if (result.allowed()) {
            println("Access granted: ${result.outcome()}")
        } else {
            println("Blocked by policy: ${result.reason()}")
        }
    }
}
```

## Architecture

```
pollux-polyglot-jvm/
├── src/main/java/org/sxnnyside/pollux/     # Public Engine API, Operation records, and exceptions
├── src/main/java/org/sxnnyside/pollux/internal/ # Native Panama FFM bridge and library resolution
├── src/test/java/org/sxnnyside/pollux/     # JUnit 5 deterministic verification suite
├── .github/workflows/                      # Continuous integration and commit validation
└── Justfile                                # Standardized developer task runner
```

## Contributing

Contributions are accepted. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

Before contributing, read the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <strong>Pollux Polyglot JVM</strong> — A Sxnnyside Project<br>
  <em>&copy; 2026 Sxnnyside Project</em>
</p>
