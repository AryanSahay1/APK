# Keep Room entities and DAOs
-keep class com.nexos.ai.data.local.entity.** { *; }

# Keep Retrofit DTOs
-keep class com.nexos.ai.data.remote.dto.** { *; }

# Gson
-keepattributes Signature, *Annotation*, InnerClasses
-keep class com.google.gson.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembernames interface * { @retrofit2.http.* <methods>; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Enums
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }
