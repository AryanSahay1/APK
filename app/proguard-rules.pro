# Keep model classes used by Gson / Retrofit
-keep class com.nexos.ai.data.remote.dto.** { *; }
-keep class com.nexos.ai.domain.model.** { *; }

# Retrofit / OkHttp
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclassmembernames interface * { @retrofit2.http.* <methods>; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Compose / Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# Enums + Parcelable
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }
-keepclassmembers class * implements android.os.Parcelable { public static final ** CREATOR; }
