import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.intellij.platform") version "2.5.0"
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
}

group   = "com.korina.cleanarch"
version = "1.0.0"

// ---------------------------------------------------------------------------
// Repositories — intellijPlatform must be added alongside mavenCentral
// ---------------------------------------------------------------------------
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// ---------------------------------------------------------------------------
// JVM target — matches the Gradle plugin module (Java 17 bytecode, no
// toolchain download required when running under JBR 21 / any JDK ≥ 17).
// ---------------------------------------------------------------------------
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// ---------------------------------------------------------------------------
// IntelliJ Platform Gradle Plugin v2 — dependency block
// ---------------------------------------------------------------------------
dependencies {
    intellijPlatform {
        // Download IntelliJ IDEA Community for building — no local IDE required.
        // This is the version used to compile against; the plugin runs on 2023.2+ (sinceBuild=232).
        intellijIdeaCommunity("2024.3.2")

        // Plugin verifier checks compatibility against target IDE versions.
        pluginVerifier()
        // Note: bundledPlugin("org.jetbrains.plugins.gradle") removed —
        // the plugin now generates files via DirectFileGenerator (plain Java IO)
        // so there is no runtime dependency on the Gradle plugin.
    }

    // kotlinx-serialization used to serialise the schema (dialog → IdeFeatureSchema).
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

// ---------------------------------------------------------------------------
// IntelliJ Platform Gradle Plugin v2 — plugin configuration block
// ---------------------------------------------------------------------------
intellijPlatform {
    // Disable Java bytecode instrumentation — not needed for a pure Kotlin plugin
    // and avoids a network download of java-compiler-ant-tasks.
    instrumentCode = false

    pluginConfiguration {
        id          = "com.korina.cleanarchitecture.idea"
        name        = "Clean Architecture Generator"
        version     = "1.0.0"
        description = """
            Generates Clean Architecture boilerplate for Android multi-module projects.
            Provides a GUI dialog to define a feature schema (name, fields, DI, storage, API)
            and writes all files directly — no Gradle plugin required.
            Generated files: Domain Model, Repository, UseCase, Room Entity,
            Retrofit DTO, Mappers, DAO, RepositoryImpl, Hilt/Koin DI module.
        """.trimIndent()
        ideaVersion {
            sinceBuild = "232"  // IntelliJ IDEA 2023.2+ — broad compatibility
            // untilBuild is intentionally omitted — plugin tracks future IDE releases.
        }
    }

    // ── Signing (required for Marketplace — configure before publishing) ─────
    // Generate a key pair: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
    // Place chain.crt and private.pem in this directory, then set PRIVATE_KEY_PASSWORD.
    signing {
        certificateChainFile.set(file("chain.crt"))
        privateKeyFile.set(file("private.pem"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    // ── Publishing ─────────────────────────────────────────────────────────────
    // Get a token: https://plugins.jetbrains.com/author/me/tokens
    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
        // Uncomment to publish to beta channel first:
        // channels.set(listOf("beta"))
    }
}

// ---------------------------------------------------------------------------
// Task configuration
// ---------------------------------------------------------------------------
tasks {
    // buildSearchableOptions launches the IDE in headless mode to index Settings entries.
    // Disabled because it requires running inside IntelliJ's JBR (not the system JVM)
    // and is not essential for plugin functionality.  Re-enable before Marketplace release.
    named("buildSearchableOptions") {
        enabled = false
    }
}