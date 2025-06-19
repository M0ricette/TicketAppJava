plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Assurez-vous que le plugin Kotlin est bien là si vous utilisez Kotlin
}

android {
    namespace = "com.example.approdrigue"
    compileSdk = 35 // Ciblez la dernière API stable si possible, 35 est bien.

    defaultConfig {
        applicationId = "com.example.approdrigue"
        minSdk = 26 // Important : Amplify 2.x REQUIERT minSdk 26 minimum.
        // Si vous aviez 24, c'est une source potentielle de problèmes.
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Recommandé pour Amplify et Java 8
        targetCompatibility = JavaVersion.VERSION_1_8 // Recommandé pour Amplify et Java 8
        isCoreLibraryDesugaringEnabled = true // Gardez ceci pour les APIs Java 8+
    }

    kotlinOptions {
        jvmTarget = "1.8" // Doit correspondre à compileOptions. JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        compose = false // Ou true si vous utilisez Compose
    }
}

dependencies {
    // AndroidX Core et AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // AndroidX Security (pour le chiffrement utilisé par Amplify)
    // Si libs.androidx.security.crypto est déjà défini dans votre version catalog, utilisez-le.
    // Sinon, utilisez la version directe comme ci-dessous.
    implementation(libs.androidx.security.crypto) // Si déjà définie dans libs.versions.toml
    // Ou directement: implementation("androidx.security:security-crypto:1.1.0-alpha06") // Vérifiez la dernière version stable ou alpha
    implementation("androidx.security:security-crypto:1.1.0-rc01")

    // AWS Amplify – Core + Cognito Auth plugin
    implementation("com.amplifyframework:core:2.9.0")
    implementation("com.amplifyframework:aws-auth-cognito:2.9.0")

    // Ajoutez aws-api car il est souvent nécessaire pour l'interaction avec les services AWS
    // et pour la récupération complète des tokens Cognito via l'API.
    implementation("com.amplifyframework:aws-api:2.9.0")

    // Google Tink (pour le chiffrement sécurisé, souvent sous le capot de AndroidX Security, mais bon à inclure)
    // Ce n'est généralement pas nécessaire si security-crypto est là, mais ne fera pas de mal.
    implementation("com.google.crypto.tink:tink-android:1.11.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout) // Vérifiez la dernière version

    // Enable Java 8+ APIs in lower API levels (D8 desugaring)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4") // Votre version actuelle est bonne.
    implementation("com.google.android.material:material:1.10.0") // Votre version actuelle est bonne.
    implementation("androidx.browser:browser:1.7.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

