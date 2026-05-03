plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.tomilov.acrobitsvoip.softphone.acrobits"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    api(project(":core:voip"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.libsoftphone)
}
