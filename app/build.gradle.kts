plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// プロパティ定義
val signalingEndpoint = project.findProperty("signaling_endpoint") as? String ?: error("signaling_endpoint is not defined")
val channelId = project.findProperty("channel_id") as? String ?: error("channel_id is not defined")
val signalingMetadata = project.findProperty("signaling_metadata") as? String ?: error("signaling_metadata is not defined")

android {
    namespace = "info.tsurutatakumi.messagingsample"
    compileSdk = 35

    defaultConfig {
        applicationId = "info.tsurutatakumi.messagingsample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SIGNALING_ENDPOINT", "\"$signalingEndpoint\"")
        buildConfigField("String", "CHANNEL_ID", "\"$channelId\"")
        buildConfigField("String", "SIGNALING_METADATA", "\"${signalingMetadata}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.sora.android.sdk) {
        artifact {
            type = "aar"
        }
        isTransitive = true
    }
    implementation(libs.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}