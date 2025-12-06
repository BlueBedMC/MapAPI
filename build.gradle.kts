plugins {
    id("java")
    id("maven-publish")
}

group = "com.bluebed"
version = "1.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven")
        maven("https://jitpack.io")
    }
}

subprojects {
    tasks.withType<Jar> {
        archiveBaseName.set("mapapi-${project.name}")
    }
}
