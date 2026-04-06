plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.nyro"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/jitsi/jcef") // JCEF Maven repo
}

dependencies {
    // JCEF for Java/Kotlin
    implementation("org.jitsi:jcef:119.0.18") 
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.nyro.browser.MainKt")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.nyro.browser.MainKt"
    }
    // Bundle dependencies into the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<CreateStartScripts>("startScripts") {
    // This creates the .exe launcher via NSIS, but we need the native libs
    // For JCEF to work, the native .dll/.so files must be accessible.
    // We copy them to the install directory.
    doLast {
        copy {
            from(configurations.runtimeClasspath.get().files.filter { it.path.contains("jcef") && !it.path.endsWith(".jar") })
            into(layout.buildDirectory.dir("install/NyroBrowser/lib"))
        }
    }
}
