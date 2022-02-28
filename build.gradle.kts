import io.gitlab.arturbosch.detekt.Detekt
import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"

    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

group = "land.vani"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
        }
    }
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/central")
    mavenLocal()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.charleskorn.kaml:kaml:0.40.0")

    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:dev-bundle:1.18.1-R0.1-SNAPSHOT")
    testCompileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")

    testImplementation("io.kotest:kotest-runner-junit5:5.1.0")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
    testImplementation("land.vani.mockpaper:MockPaper-1.18.1:1.0.1")
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "$targetJavaVersion"
        }
    }

    withType<Test> {
        useJUnitPlatform()
        maxParallelForks = Runtime.getRuntime().availableProcessors() / 2 + 1

        extensions.configure<KoverTaskExtension> {
            isDisabled = false
            includes = listOf("land.vani.mockpaper.*")
        }
    }

    withType<Detekt> {
        reports {
            xml.required.set(true)
        }
        jvmTarget = "16"
    }

    koverXmlReport {
        isEnabled = true
    }
}
