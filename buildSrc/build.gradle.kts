import java.util.Properties

plugins {
    `kotlin-dsl`
    idea
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
    }
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://repo.spongepowered.org/repository/maven-public")
    maven("https://maven.parchmentmc.org")
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}

gradlePlugin {
    plugins {
        create("platformPlugin") {
            id = "flywheel.platform"
            implementationClass = "dev.engine_room.gradle.platform.PlatformPlugin"
        }
        create("subprojectPlugin") {
            id = "flywheel.subproject"
            implementationClass = "dev.engine_room.gradle.subproject.SubprojectPlugin"
        }
    }
}

val properties by lazy {
    Properties().apply {
        load(rootDir.parentFile.resolve("gradle.properties").inputStream())
    }
}

dependencies {
    implementation("net.fabricmc.fabric-loom-companion:net.fabricmc.fabric-loom-companion.gradle.plugin:${properties["loom_version"]}")
}
