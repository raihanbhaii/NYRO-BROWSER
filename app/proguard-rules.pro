# =============================================
# SnakeYAML + java.beans fixes (Android R8)
# =============================================
-dontwarn java.beans.**
-keep class java.beans.** { *; }

# Keep all SnakeYAML classes (prevents reflection issues)
-keep class org.yaml.snakeyaml.** { *; }
-dontwarn org.yaml.snakeyaml.**

# Optional: Keep your own extension-related classes
-keep class com.nyro.browser.extensions.** { *; }
-keep class com.nyro.browser.utils.** { *; }

# =============================================
# Default recommended rules
# =============================================
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
