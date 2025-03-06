plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
    maven { url = uri("https://maven.google.com") }
}

dependencies {
//    implementation("com.android.tools:common:30.4.0")
    implementation("com.google.code.gson:gson:2.8.9")
    compileOnly(files("${System.getenv("ANDROID_SDK_ROOT")}/platform-tools/adb"))
    implementation("com.android.tools.ddms:ddmlib:26.5.0")
    implementation("com.android.tools.idea:android:2024.2.2.13")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version = "2024.2.2"
    plugins.set(listOf())
    updateSinceUntilBuild = false
    sameSinceUntilBuild = false
}

tasks.runIde {
    ideDir.set(file("/Applications/Android Studio.app/Contents"))
}


tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
