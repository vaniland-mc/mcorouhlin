plugins {
    kotlin("jvm") version "1.8.20"

    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.0.11")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.22.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.10")
}

gradlePlugin {
    plugins {
        register("setupKotlinPlugin") {
            id = "land.vani.setup.kotlin"
            implementationClass = "land.vani.setup.kotlin.SetupKotlinPlugin"
        }
        register("setupMavenPublishPlugin") {
            id = "land.vani.setup.maven"
            implementationClass = "land.vani.setup.maven.SetupMavenPublicationPlugin"
        }
    }
}
