# ================== KEEP KOTLIN SERIALIZATION ==================
-keepattributes *Annotation*,InnerClasses,Signature
-keepclassmembers class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ================== GECKOVIEW (CRITICAL) ==================
-keep class org.mozilla.geckoview.** { *; }
-keepclassmembers class org.mozilla.geckoview.** { *; }
-dontwarn org.mozilla.geckoview.**

# ================== YOUR APP CLASSES ==================
-keep class com.nyro.browser.** { *; }
-keepclassmembers class com.nyro.browser.** { *; }

# Keep models used in serialization / extensions
-keep class com.nyro.browser.extensions.models.** { *; }
-keepclassmembers class com.nyro.browser.extensions.models.** { *; }

# ================== HILT & DEPENDENCY INJECTION ==================
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.** { *; }
-keep @javax.inject.** class * { *; }
-dontwarn javax.inject.**

# ================== OKHTTP & NETWORKING ==================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ================== ANDROIDX & CORE ==================
-keep class androidx.** { *; }
-dontwarn androidx.**

# ================== NATIVE & PARCELABLE ==================
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ================== SERIALIZABLE ==================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================== REMOVE LOGGING IN RELEASE ==================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
