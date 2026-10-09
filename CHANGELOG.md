# Changelog

All notable changes to **Pollux Polyglot JVM** are documented here.

This project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

---

## [0.1.0] — 2026-10-09

### Added

- Native Java 22+/23+ binding for Pollux Core via Project Panama (Foreign Function & Memory API / JEP 454).
- Safe, deterministic resource management via `AutoCloseable` (`try-with-resources` in Java, `.use { }` in Kotlin).
- Strongly-typed `Operation` models and builders for Filesystem, Network, Process, Secret, Env, and Device domains.
- Full protocol and evaluation trace deserialization via `EvaluationResult`.
- Comprehensive JUnit 5 test suite validating ABI versioning, life-cycle, and deterministic evaluation.
- Standard Sxnnyside quality gates, `Justfile` tasks, and CI workflows.

---

[Unreleased]: https://github.com/sxnnyside-project/pollux-polyglot-jvm/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sxnnyside-project/pollux-polyglot-jvm/releases/tag/v0.1.0
