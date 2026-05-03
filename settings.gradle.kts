pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    val acrobitsMavenUsername = providers.gradleProperty("acrobitsMavenUsername")
        .orElse(providers.environmentVariable("ACROBITS_MAVEN_USERNAME"))
        .orElse("net.acrobits.interview.test.android")
        .get()
    val acrobitsMavenPassword = providers.gradleProperty("acrobitsMavenPassword")
        .orElse(providers.environmentVariable("ACROBITS_MAVEN_PASSWORD"))
        .orElse("")
        .get()

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.acrobits.net/repository/maven-releases/")
            credentials {
                username = acrobitsMavenUsername
                password = acrobitsMavenPassword
            }
        }
    }
}

rootProject.name = "AcrobitsVoip"
include(":app")
