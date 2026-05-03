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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.libsoftphone)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
