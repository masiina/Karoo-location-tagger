pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // Local maven repo (karoo-ext AAR from cache, no auth needed)
        maven {
            url = uri("${rootProject.projectDir}/local-maven-repo")
        }
        // karoo-ext from Github Packages (requires gpr.user / gpr.key)
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("USERNAME") ?: "dummy")
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("TOKEN") ?: "dummy")
            }
        }
    }
}

rootProject.name = "Karoo Location Tagger"
include("app")