pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NudgeKit"

include(":nudgekit-core")
include(":nudgekit-datastore")
include(":nudgekit-compose")
include(":nudgekit-compose-datastore")
include(":sample")
