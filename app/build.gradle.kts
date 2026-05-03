plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun String.toBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

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
            providers.gradleProperty("acrobitsLicenseKey")
                .orElse(providers.environmentVariable("ACROBITS_LICENSE_KEY"))
                .orElse("")
                .get()
                .toBuildConfigString()
        )
        buildConfigField("String", "SIP_HOST", "pbx.acrobits.cz".toBuildConfigString())
        buildConfigField(
            "String",
            "DEFAULT_SIP_USERNAME",
            providers.gradleProperty("defaultSipUsername")
                .orElse(providers.environmentVariable("DEFAULT_SIP_USERNAME"))
                .orElse("")
                .get()
                .toBuildConfigString()
        )
        buildConfigField(
            "String",
            "DEFAULT_SIP_PASSWORD",
            providers.gradleProperty("defaultSipPassword")
                .orElse(providers.environmentVariable("DEFAULT_SIP_PASSWORD"))
                .orElse("")
                .get()
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
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
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
