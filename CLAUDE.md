# Pollux Polyglot JVM Guide

## Overview
`pollux-polyglot-jvm` is the official Java Virtual Machine (JVM) language binding for Pollux Core and the Pollux FFI Native Bridge (`pollux-abi/1`).
Compatible seamlessly across Java (Java 22+/23+), Kotlin, Scala, Groovy, and Eclipse/IntelliJ runtimes using modern OpenJDK Project Panama (Foreign Function & Memory API, JEP 454) without legacy JNI overhead.

## Commands

Always use `just` recipes when interacting with this repository:

- `just install` - Resolve and download Gradle dependencies
- `just dev` - Fast compilation of Java source classes
- `just build` - Compile and package distribution JARs
- `just test` - Run full JUnit 5 test suite
- `just typecheck` - Run strict Java compiler type checks
- `just lint` - Run compiler linting flags (`-Xlint:all`)
- `just format-check` - Verify code style and formatting standards
- `just check` - Run non-mutating quality gate (`typecheck` + `test`)
- `just publish-local` - Publish to local Maven cache (~/.m2/repository)
- `just publish` - Publish signed artifacts to Maven Central
- `just clean` - Clean Gradle build artifacts and temporary files

## Architecture Rules

- **Zero Legacy JNI**: Modern Panama Foreign Function & Memory (FFM) API replaces manual C glue, `javah`, and brittle native library loaders.
- **Strict Typing & Immutability**: All domain inputs and output verdicts use modern Java records (`Operation`, `EvaluationResult`).
- **Deterministic Resource Disposal**: Engine instances implement `AutoCloseable`. Native pointers must be released deterministically with `close()`, `try-with-resources` (Java) or `.use { }` (Kotlin).
- **Error Propagation**: Return status codes from native C functions (`PolluxStatus`) are mapped to typed domain exceptions (`PolluxException`).
- **Binary Compatibility**: Validates `pollux-abi/1` protocol on engine instantiation to prevent ABI drift.
