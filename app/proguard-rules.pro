# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# WebRTC оптимизации
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Room оптимизации
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* *;
}

# Hilt оптимизации
-keep class * extends dagger.hilt.android.internal.GeneratedComponentManagerHolder
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keepclassmembers class * {
    @dagger.hilt.* *;
}

# Retrofit/OkHttp оптимизации
-keep class com.example.aagnar.data.remote.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# WebSocket оптимизации
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# Критические классы приложения
-keep class com.example.aagnar.presentation.ui.** { *; }
-keep class com.example.aagnar.domain.model.** { *; }
-keep class com.example.aagnar.data.repository.** { *; }

# Убираем логи в релизной сборке
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}