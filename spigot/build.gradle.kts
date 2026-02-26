plugins {
    java
    id("maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "com.bluebed.mapapi"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(project(":api-core"))

    //spigot
    compileOnly("org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT")

    implementation("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    annotationProcessor("org.jetbrains:annotations:26.0.2")
    implementation("org.jetbrains:annotations:26.0.2")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.bluebed.mapapi"
            artifactId = project.name
            version = "${project.version}"
        }
    }
    repositories {
        mavenLocal()
    }
}