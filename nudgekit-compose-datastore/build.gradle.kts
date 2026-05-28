plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
    signing
}

group = providers.gradleProperty("nudgekitGroup").get()
version = providers.gradleProperty("nudgekitVersion").get()

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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

// The Android `release` component only exists after evaluation.
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "nudgekit-compose-datastore"
                pom {
                    name.set("NudgeKit Compose DataStore")
                    description.set(
                        "State-aware managed Compose components for NudgeKit — " +
                            "ManagedInlineTip and ManagedTipBox, wired to DataStore persistence.",
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
