plugins {
    alias(libs.plugins.android.library)
}

group = "com.korina.myapp"

android {
    namespace  = "com.korina.myapp.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.android)
}
