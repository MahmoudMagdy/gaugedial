pluginManagement {
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

rootProject.name = "Gauge Dial"
include(":app")
include(":gaugedialinformationlib")
include(":gaugedialinformation")
include(":common")
include(":gaugedialinformationmanager")
