plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.1"
}

group = "com.john"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

tasks.shadowJar {
    mergeServiceFiles()       // Important for libraries like Jackson that use ServiceLoaders
}

tasks.test {
    useJUnitPlatform()
}