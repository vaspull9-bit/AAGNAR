plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
}

android {
    namespace = "com.example.aagnar"
    compileSdk = 34

    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.aagnar"
        applicationId = "com.example.aagnar"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "4.0.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Включаем сжатие ресурсов
            isCrunchPngs = true
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Включаем десериализацию
        isCoreLibraryDesugaringEnabled = true

    }

    kotlinOptions {
        jvmTarget = "17"
    }



    buildFeatures {
        viewBinding = false
        dataBinding = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/androidx.customview_customview.version"
            excludes += "META-INF/*.version"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

// kapt конфигурация (один раз!)
//kapt {
//    correctErrorTypes = true
//}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // UI
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2.v123)
    implementation(libs.androidx.camera.lifecycle.v123)
    implementation(libs.androidx.camera.view.v123)
    implementation(libs.androidx.media)

    // Network
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Security
    implementation(libs.androidx.security.crypto)
    //  implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Preference
    implementation(libs.androidx.preference.ktx)

    // Document File
    implementation(libs.androidx.documentfile)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // WebSocket
    implementation(libs.java.websocket)
        // implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // QR Code
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // JSON
    implementation("org.json:json:20231013")

    // WebRTC
    implementation(libs.webrtc.android)

    // Permissions
    implementation("com.github.florent37:runtime-permission-kotlin:1.1.2")

}
