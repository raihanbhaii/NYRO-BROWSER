# Keep Kotlin serialization
-keepattributes *Annotation*,InnerClasses
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep GeckoView classes
-keep class org.mozilla.geckoview.** { *; }
-dontwarn org.mozilla.geckoview.**

# Keep extension API bridge
-keep class com.nyro.browser.extensions.** { *; }
-keepclassmembers class com.nyro.browser.extensions.** { *; }

# Keep models for serialization
-keep class com.nyro.browser.extensions.models.** { *; }
-keepclassmembers class com.nyro.browser.extensions.models.** { *; }

# Keep Hilt components
-keep class * extends dagger.hilt.android.** { *; }
-keep class * implements javax.inject.** { *; }
-keep @javax.inject.** class * { *; }
-dontwarn javax.inject.**

# Keep AndroidX classes
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep OkHttp/Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep JSON serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }

# Keep extension manifest parsing
-keepclassmembers class com.nyro.browser.extensions.ManifestParser { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# Keep serialization UIDs
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
