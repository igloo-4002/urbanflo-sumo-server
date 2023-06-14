import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
}

group = "app.urbanflo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    // sumo repositories
    maven {
        url = uri("https://repo.eclipse.org/content/repositories/sumo-releases")
    }
    maven {
        url = uri("https://repo.eclipse.org/content/repositories/sumo-snapshots/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-29")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.eclipse.sumo:libsumo:1.18.0-SNAPSHOT")
    implementation("org.eclipse.sumo:libtraci:1.18.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
