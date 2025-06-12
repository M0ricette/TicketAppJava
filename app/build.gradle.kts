plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.approdrigue"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.approdrigue"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = false
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.security.crypto)

    // AWS Amplify – Core + Cognito Auth plugin
    implementation("com.amplifyframework:core:2.27.4")              // :contentReference[oaicite:0]{index=0}
    implementation("com.amplifyframework:aws-auth-cognito:2.27.4") // :contentReference[oaicite:1]{index=1}

    // Enable Java 8+ APIs in lower API levels
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.google.android.material:material:1.10.0")
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
