plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}



android {
    namespace = "com.example.approdrigue"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.approdrigue"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        // Explicitly disable Compose to avoid requesting the Compose compiler
        compose = false

    }


}

dependencies {
    implementation ("androidx.compose.ui:ui:1.5.3")
    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")
    implementation("androidx.compose.runtime:runtime:1.5.3")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.security.crypto)
    implementation(libs.amplify.core)
    implementation(libs.amplify.auth.cognito)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}