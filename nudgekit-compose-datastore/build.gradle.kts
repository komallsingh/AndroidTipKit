plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    // Distinct Android namespace (governs the generated R/BuildConfig class).
    // The Kotlin package of the components stays `dev.nudgekit.compose` so that
    // consumer imports (e.g. `dev.nudgekit.compose.ManagedInlineTip`) are unchanged.
    namespace = "dev.nudgekit.compose.datastore"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":nudgekit-core"))
    api(project(":nudgekit-datastore"))
    api(project(":nudgekit-compose"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.coroutines.android)
}
