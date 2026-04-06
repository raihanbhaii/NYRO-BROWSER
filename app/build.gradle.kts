// 1. Plugins MUST go first
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") 
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") // Add this for JSON serialization
}

// 2. Android configuration goes second
android {
    // This defines where your R file and BuildConfig are generated
    namespace = "com.nyro.browser" 
    compileSdk = 34

    defaultConfig {
        // This is your unique ID on the Google Play Store
        applicationId = "com.nyro.browser" 
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        buildConfig = true
        dataBinding = true  // ✅ CRITICAL - enables ViewBinding and DataBinding
        viewBinding = true  // ✅ Also enable view binding
    }
}

// 3. Dependencies MUST go last
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // GeckoView
    implementation("org.mozilla.geckoview:geckoview:124.0.20240311145044") 

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.google.code.gson:gson:2.10.1")  // Add GSON for JSON parsing
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // Desugaring for Java 8+ features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Allow Hilt to work with Kotlin
kapt {
    correctErrorTypes = true
}
