import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun String.toBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use { load(it) }
    }
}

fun configValue(
    gradlePropertyName: String,
    environmentVariableName: String
): String = providers.gradleProperty(gradlePropertyName)
    .orElse(providers.environmentVariable(environmentVariableName))
    .orElse(localProperties.getProperty(gradlePropertyName) ?: "")
    .get()

fun requiredConfigValue(
    gradlePropertyName: String,
    environmentVariableName: String,
    displayName: String
): String = configValue(gradlePropertyName, environmentVariableName).also { value ->
    if (value.isBlank()) {
        throw GradleException(
            "Missing $displayName. Set $gradlePropertyName in local.properties or " +
                "~/.gradle/gradle.properties, pass -P$gradlePropertyName=..., or set " +
                "$environmentVariableName."
        )
    }
}

android {
    namespace = "com.tomilov.acrobitsvoip"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "net.acrobits.interview.test.android"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "ACROBITS_LICENSE_KEY",
            requiredConfigValue(
                gradlePropertyName = "acrobitsLicenseKey",
                environmentVariableName = "ACROBITS_LICENSE_KEY",
                displayName = "Acrobits runtime license key"
            )
                .toBuildConfigString()
        )
        buildConfigField("String", "SIP_HOST", "pbx.acrobits.cz".toBuildConfigString())
        buildConfigField(
            "String",
            "DEFAULT_SIP_USERNAME",
            configValue(
                gradlePropertyName = "defaultSipUsername",
                environmentVariableName = "DEFAULT_SIP_USERNAME"
            )
                .toBuildConfigString()
        )
        buildConfigField(
            "String",
            "DEFAULT_SIP_PASSWORD",
            configValue(
                gradlePropertyName = "defaultSipPassword",
                environmentVariableName = "DEFAULT_SIP_PASSWORD"
            )
                .toBuildConfigString()
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":core:designsystem"))
    implementation(project(":core:voip"))
    implementation(project(":feature:calling"))
    implementation(project(":softphone:acrobits"))
    implementation(libs.androidx.activity.compose)
}
