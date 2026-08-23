plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.deepnight.sdk.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.deepnight.sdk.sample"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":tv-input"))
    implementation(project(":dap-core"))
    implementation(project(":ai-commands"))
    implementation(project(":text-tools"))
    implementation(project(":tv-ui-kit"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.activity.compose)
}
