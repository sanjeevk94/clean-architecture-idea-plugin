plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.korina.myapp"

// Target Java 11 bytecode — matches the only installed JDK (11.0.13).
// Do NOT use jvmToolchain(N) as it triggers toolchain discovery/download.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
}

