plugins {
    kotlin("jvm") version "1.7.0"

    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:2.3.3")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.20.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.21")
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
