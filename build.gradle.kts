plugins {
    kotlin("jvm") version "1.9.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" // Fat JAR plugin
}

group = "com.nyro"
version = System.getenv("GITHUB_RUN_NUMBER") ?: "1.0.0" // Auto-version from CI

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/jitsi/jcef")
}

dependencies {
    implementation("org.jitsi:jcef:119.0.18")
    implementation("com.google.code.gson:gson:2.10.1") // For config handling
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.nyro.browser.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Djava.awt.headless=false",
        "-Dsun.java2d.d3d=true" // Enable Direct3D for better rendering
    )
}

// Create fat JAR with all dependencies
tasks.shadowJar {
    archiveBaseName.set("nyro-browser")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

// Configure installDist to include JCEF natives
tasks.installDist {
    doLast {
        // Copy JCEF native libraries to the lib folder
        val jcefNatives = configurations.runtimeClasspath.get().files.filter { 
            it.name.contains("jcef") && it.extension != "jar" 
        }
        copy {
            from(jcefNatives)
            into(layout.buildDirectory.dir("install/NyroBrowser/lib"))
        }
    }
}
