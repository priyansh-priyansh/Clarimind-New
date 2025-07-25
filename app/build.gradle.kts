plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
    id("kotlin-kapt") // <-- Add this line for Room annotation processing
}

android {
    namespace = "com.example.clarimind"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.clarimind"
        minSdk = 24
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
        compose = true
        viewBinding = true
        mlModelBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    
    // AppCompat for traditional View-based activities
    implementation("androidx.appcompat:appcompat:1.7.1")
    
    // Material Design for View-based layouts
    implementation("com.google.android.material:material:1.12.0")
    
    // Kotlinx Serialization core
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Gemini AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Navigation for Jetpack Compose
    implementation(libs.androidx.navigation.compose)

    // CameraX core libraries
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2.v131)

// Lifecycle support
    implementation(libs.androidx.camera.lifecycle.v131)

// CameraX Preview, ImageCapture, and VideoCapture use cases
    implementation(libs.androidx.camera.view.v131)
    implementation(libs.androidx.camera.extensions)

    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.exifinterface)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.6.3")
    }
}