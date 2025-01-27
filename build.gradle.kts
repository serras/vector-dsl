plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.serranofp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
    compilerOptions {
        freeCompilerArgs.add("-Xadd-modules=jdk.incubator.vector")
    }
}