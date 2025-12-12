pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Daitso"
include(":app")
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:ui")
include(":feature:home")
include(":feature:detail")
