import java.util.Properties

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

val localProperties = Properties().apply {
    val file = file("local.properties")
    if (file.isFile) {
        file.inputStream().use { load(it) }
    }
}

fun requiredConfigValue(
    gradlePropertyName: String,
    environmentVariableName: String,
    displayName: String
): String {
    val value = providers.gradleProperty(gradlePropertyName)
        .orElse(providers.environmentVariable(environmentVariableName))
        .orElse(localProperties.getProperty(gradlePropertyName) ?: "")
        .get()
        .trim()

    if (value.isBlank()) {
        throw GradleException(
            "Missing $displayName. Set $gradlePropertyName in local.properties or " +
                "~/.gradle/gradle.properties, pass -P$gradlePropertyName=..., or set " +
                "$environmentVariableName."
        )
    }

    return value
}

dependencyResolutionManagement {
    val acrobitsMavenUsername = requiredConfigValue(
        gradlePropertyName = "acrobitsMavenUsername",
        environmentVariableName = "ACROBITS_MAVEN_USERNAME",
        displayName = "Acrobits Maven username"
    )
    val acrobitsMavenPassword = requiredConfigValue(
        gradlePropertyName = "acrobitsMavenPassword",
        environmentVariableName = "ACROBITS_MAVEN_PASSWORD",
        displayName = "Acrobits Maven password"
    )

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
