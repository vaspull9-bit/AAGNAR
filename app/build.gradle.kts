// C:\Users\trii\AndroidStudioProjects\AAGNAR\app\build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
    id("com.google.devtools.ksp")    // ← ДОБАВИТЬ для Room
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
        versionName = "4.0.7"
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

    coreLibraryDesugaring(libs.desugar.jdk.libs)

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
    ksp(libs.androidx.room.compiler)

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
    implementation(libs.json)

    // WebRTC
   // implementation(libs.webrtc.android)
   //  implementation(libs.google.webrtc)
   // implementation(libs.webrtc)
   //implementation("org.pion:webrtc:0.0.0")    // Permissions
   // implementation(libs.runtime.permission.kotlin)
    // implementation("io.livekit:livekit-webrtc:0.10.0")
   // implementation("com.github.dbakhtin:webrtc-android:1.0.0")
        // implementation("com.github.react-native-webrtc:webrtc:107.0.0")
    implementation("io.getstream:stream-webrtc-android:1.1.1")

    //implementation("com.github.florent37:runtime-permission:1.1.2")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    implementation("de.hdodenhof:circleimageview:3.1.0")
}
