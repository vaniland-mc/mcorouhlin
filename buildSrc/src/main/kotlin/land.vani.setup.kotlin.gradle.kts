import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

val targetJavaVersion = 17

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
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

        extensions.configure<KoverTaskExtension> {
            isDisabled = false
        }
    }

    koverXmlReport {
        isEnabled = true
    }

    tasks.withType<Detekt> {
        jvmTarget = "$targetJavaVersion"
        reports {
            xml.required.set(true)
            sarif.required.set(true)
        }

        val sarifReportMerge by rootProject.tasks.getting(ReportMergeTask::class)
        finalizedBy(sarifReportMerge)
        sarifReportMerge.input.from(sarifReportFile)

        val xmlReportMerge by rootProject.tasks.getting(ReportMergeTask::class)
        finalizedBy(xmlReportMerge)
        xmlReportMerge.input.from(xmlReportFile)
    }
}

detekt {
    parallel = true
    config = rootProject.files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("1.6.10")
            }
        }
    }
}
