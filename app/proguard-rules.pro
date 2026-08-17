# Kahani Proguard Rules

# Firebase common rules
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }

# Firestore model rules (to prevent obfuscation of field names used in mapping)
-keepclassmembers class com.vl.kahani.data.Series { <fields>; }
-keepclassmembers class com.vl.kahani.data.Chapter { <fields>; }
-keepclassmembers class com.vl.kahani.data.ReadingProgress { <fields>; }
-keepclassmembers class com.vl.kahani.data.CoinTransaction { <fields>; }
-keepclassmembers class com.vl.kahani.data.AppNotification { <fields>; }
-keepclassmembers class com.vl.kahani.data.DownloadedChapter { <fields>; }
-keepclassmembers class com.vl.kahani.data.Review { <fields>; }

# Coil
-keep class coil.** { *; }

# Compose
-keep class androidx.compose.** { *; }
