plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("com.android.application") version "8.2.1" //בשביל אפליקצייה
//    id("com.android.library") version "8.8.0" // בשביל ספרייה
    id("org.jetbrains.kotlin.android") version "2.1.10" // עדכן לגרסה העדכנית המומלצת
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {

}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.2.4") // IntelliJ Community Edition
    type.set("AI") // גרסה תואמת ל-Android Studio
    plugins.set(listOf("android")) // תוסף Android
}

tasks {
    // Set the JVM compatibility versions
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
