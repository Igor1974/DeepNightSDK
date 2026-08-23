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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DeepNightSDK"

include(":tv-input")
include(":dap-core")
include(":ai-commands")
include(":text-tools")
include(":tv-ui-kit")
include(":sample-app")
