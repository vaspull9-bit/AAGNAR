

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
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
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a") // Только современные устройства
            // abiFilters.add("armeabi-v7a") // Раскомментируй если нужна поддержка старых
        }

//        splits {
//            abi {
//                isEnable = true
//                reset()
//                include("armeabi-v7a", "arm64-v8a") // Только эти архитектуры
//                isUniversalApk = false
//            }
//        }


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
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }


    kapt {
        correctErrorTypes = true
        javacOptions {
            option("-source", "17")
            option("-target", "17")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
       viewBinding = false
        dataBinding = false // ЗАКОММЕНТИРОВАТЬ для решения ошибки
        // buildConfig = true
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


kapt {
    correctErrorTypes = true
}


dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

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

    // Camera & Media
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.media)

    // Network
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
       kapt(libs.androidx.room.compiler)  // Используем kapt вместо KSP

    // Security
    implementation(libs.androidx.security.crypto)


     // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Preference
    implementation(libs.androidx.preference.ktx)

    // Document File
    implementation(libs.androidx.documentfile)

    // HILT
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


// WebSocket для signaling
    implementation(libs.java.websocket)

    // WebRTC
    // implementation(libs.google.webrtc)
   // implementation(libs.google.webrtc)

 }
