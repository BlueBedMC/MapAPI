plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

val minestomVersion = "2025.10.05-1.21.8"

group = "com.bluebed.mapapi"
version = "1.0-$minestomVersion"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":api-core"))

    implementation("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("net.minestom:minestom:$minestomVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
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
