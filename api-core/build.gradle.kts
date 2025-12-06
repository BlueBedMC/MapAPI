plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

group = "com.bluebed.mapapi"
version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
}

val lwjglVersion = "3.2.3"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // lombok
    implementation("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // lwjgl
    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    api("org.lwjgl:lwjgl")
    api("org.lwjgl:lwjgl-egl")
    api("org.lwjgl:lwjgl-opengl")
    api("org.lwjgl:lwjgl-opengles")
    api("org.lwjgl:lwjgl-glfw")
    api("org.lwjgl:lwjgl-glfw")
    api("org.lwjgl:lwjgl-stb")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}