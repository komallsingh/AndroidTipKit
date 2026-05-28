plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = providers.gradleProperty("nudgekitGroup").get()
version = providers.gradleProperty("nudgekitVersion").get()

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "nudgekit-core"
            pom {
                name.set("NudgeKit Core")
                description.set(
                    "Android-free rule engine and models for NudgeKit — contextual " +
                        "tips, feature discovery, and onboarding nudges.",
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
