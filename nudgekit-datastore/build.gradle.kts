plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

group = providers.gradleProperty("nudgekitGroup").get()
version = providers.gradleProperty("nudgekitVersion").get()

android {
    namespace = "dev.nudgekit.datastore"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api(project(":nudgekit-core"))
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}

// The Android `release` component only exists after evaluation.
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "nudgekit-datastore"
                pom {
                    name.set("NudgeKit DataStore")
                    description.set(
                        "AndroidX DataStore Preferences persistence for NudgeKit — " +
                            "DataStoreTipManager plus reactive state and evaluation helpers.",
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
