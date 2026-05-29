plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
    signing
    alias(libs.plugins.dokka)
}

group = providers.gradleProperty("nudgekitGroup").get()
version = providers.gradleProperty("nudgekitVersion").get()

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Real API docs (Dokka, javadoc format) packaged as the -javadoc.jar required
// by Maven Central. Output dir is build/dokka/javadoc. (AGP's own
// withJavadocJar() runs a Dokka worker that fails on these Compose sources;
// the standalone Dokka plugin handles them fine.)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/javadoc"))
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

// The Android `release` component only exists after evaluation.
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(javadocJar)
                artifactId = "nudgekit-compose"
                pom {
                    name.set("NudgeKit Compose")
                    description.set(
                        "Pure Material 3 Compose UI for NudgeKit — InlineTip, TipBox, " +
                            "and styling. No DataStore dependency.",
                    )
                    url.set("https://github.com/Abdullajon1881/AndroidTipKit")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("Abdullajon1881")
                            name.set("Abdullajon1881")
                            url.set("https://github.com/Abdullajon1881")
                        }
                    }
                    scm {
                        url.set("https://github.com/Abdullajon1881/AndroidTipKit")
                        connection.set("scm:git:https://github.com/Abdullajon1881/AndroidTipKit.git")
                        developerConnection.set("scm:git:ssh://git@github.com/Abdullajon1881/AndroidTipKit.git")
                    }
                }
            }
        }
    }

    // In-memory PGP signing, gated so it only activates when key material is
    // supplied. Without keys, signing is skipped so builds/CI stay green.
    signing {
        val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
        val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull
        isRequired = signingKey != null
        if (signingKey != null) {
            val signingKeyId = providers.gradleProperty("signingInMemoryKeyId").orNull
            if (signingKeyId != null) {
                useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            } else {
                useInMemoryPgpKeys(signingKey, signingPassword)
            }
            sign(publishing.publications)
        }
    }
}
