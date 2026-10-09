# Pollux Polyglot JVM task runner.
# Every recipe wraps canonical Gradle commands.

# Bootstrap dependencies.
install:
    ./gradlew dependencies --no-daemon

# Fast dev compilation of main classes.
dev:
    ./gradlew classes --no-daemon

# Compile and produce distribution JARs.
build:
    ./gradlew assemble --no-daemon

# Run JUnit 5 test suite.
test:
    ./gradlew test --no-daemon

# Verify types and static compilation without running tests.
typecheck:
    ./gradlew compileJava compileTestJava --no-daemon

# Static analysis and linting.
lint:
    ./gradlew compileJava -Xlint:all --no-daemon

# Check formatting and style.
format-check:
    ./gradlew compileJava --no-daemon

# Full non-mutating quality gate; invoked by CI.
check: typecheck test

# Publish to local Maven cache (~/.m2/repository).
publish-local:
    ./gradlew publishToMavenLocal --no-daemon

# Publish to Maven Central (Sonatype Portal).
publish:
    ./gradlew publish --no-daemon

# Clean build directory and caches.
clean:
    ./gradlew clean --no-daemon
    rm -rf build lib/
