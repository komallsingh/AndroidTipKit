plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "dev.nudgekit.compose"
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

    // Robolectric needs access to merged Android resources for Compose UI tests.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// Compose UI tests rely on the debug-only ui-test-manifest (which registers the
// empty test Activity used by createComposeRule()). The release unit-test variant
// has no such manifest, so we only run unit tests against debug.
androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        variant.enableUnitTest = false
    }
}

dependencies {
    api(project(":nudgekit-core"))
    implementation(project(":nudgekit-datastore"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // ── Test dependencies (local JVM via Robolectric) ─────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(composeBom)
    testImplementation(libs.compose.ui.test.junit4)
    // ui-test-manifest provides the empty test Activity used by createComposeRule()
    // and must be in debug configuration so Robolectric picks it up.
    debugImplementation(libs.compose.ui.test.manifest)
}
