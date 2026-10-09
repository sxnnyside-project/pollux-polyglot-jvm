import java.util.Properties

plugins {
    `java-library`
    `maven-publish`
    signing
}

// Load .env if present
val envFile = file(".env")
if (envFile.exists()) {
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val parts = trimmed.split("=", limit = 2)
            val key = parts[0].trim()
            val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")
            if (System.getProperty(key) == null) {
                System.setProperty(key, value)
            }
        }
    }
}

fun getSecret(key: String): String? {
    return (findProperty(key) as? String)
        ?: System.getenv(key)
        ?: System.getProperty(key)
}

group = "com.sxnnysideproject"
version = "0.1.0"
description = "Deterministic Execution Authority and Sandboxing SDK for the Java Virtual Machine (JVM)"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Zero-overhead JSON parser for ABI wire protocols
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial", "-Xlint:-restricted"))
}

tasks.javadoc {
    val options = options as? StandardJavadocDocletOptions
    options?.apply {
        encoding = "UTF-8"
        docEncoding = "UTF-8"
        charSet = "UTF-8"
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    // Allow native memory access for Panama Foreign Function & Memory API
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.sxnnysideproject"
            artifactId = "pollux-polyglot-jvm"
            version = project.version.toString()

            pom {
                name.set("pollux-polyglot-jvm")
                description.set("Deterministic Execution Authority JVM SDK for Pollux Core")
                url.set("https://sxnnysideproject.com")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("HoujouSxnnyside")
                        name.set("Sxnnyside Project")
                        email.set("houjou.sxnnyside@sxnnysideproject.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sxnnyside-project/pollux-polyglot-jvm.git")
                    developerConnection.set("scm:git:ssh://github.com:sxnnyside-project/pollux-polyglot-jvm.git")
                    url.set("https://github.com/sxnnyside-project/pollux-polyglot-jvm")
                }
            }
        }
    }

    repositories {
        val sonatypeUsername = getSecret("MAVEN_CENTRAL_USERNAME") ?: getSecret("sonatypeUsername")
        val sonatypePassword = getSecret("MAVEN_CENTRAL_PASSWORD") ?: getSecret("sonatypePassword")

        if (!sonatypeUsername.isNullOrBlank() && !sonatypePassword.isNullOrBlank()) {
            maven {
                name = "SonatypeCentral"
                // Central Portal / OSSRH staging endpoint
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
}

// GPG Signing for Maven Central verification
val rawSigningKey = getSecret("SIGNING_SECRET_KEY") ?: getSecret("signingKey")
val signingKey = rawSigningKey?.replace("\\n", "\n")
val signingPassword = getSecret("SIGNING_PASSWORD") ?: getSecret("signingPassword")

if (!signingKey.isNullOrBlank()) {
    signing {
        if (!signingPassword.isNullOrBlank()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        } else {
            useInMemoryPgpKeys(signingKey, "")
        }
        sign(publishing.publications["mavenJava"])
    }
}
