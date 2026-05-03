// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

val architectureSourceFiles = files(
    "app",
    "core",
    "feature",
    "softphone"
).asFileTree.matching {
    include("**/*.kt")
    exclude("**/build/**")
}

val checkArchitecture by tasks.registering {
    group = "verification"
    description = "Verifies module boundary import rules."

    inputs.files(architectureSourceFiles)

    doLast {
        val rootDirectory = layout.projectDirectory.asFile
        val violations = architectureSourceFiles.files.flatMap { file ->
            val relativePath = file.relativeTo(rootDirectory).path.replace(File.separatorChar, '/')
            val content = file.readText()

            buildList {
                if ("cz.acrobits" in content && !relativePath.startsWith("softphone/acrobits/")) {
                    add("$relativePath imports Acrobits SDK outside :softphone:acrobits")
                }

                if ("BuildConfig" in content && !relativePath.startsWith("app/")) {
                    add("$relativePath reads BuildConfig outside :app")
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                violations.joinToString(
                    separator = System.lineSeparator(),
                    prefix = "Architecture boundary violations:" + System.lineSeparator()
                )
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkArchitecture)
}
