package land.vani.setup.kotlin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class SetupKotlinPlugin : Plugin<Project> {
    companion object {
        private const val JVM_VERSION = 17
    }

    override fun apply(project: Project) {
        applyPlugins(project)
        applyDependencies(project)
        applyJvmVersions(project)
        applyTestConfig(project)
        applyDetekt(project)
    }

    private fun applyPlugins(project: Project) {
        project.plugins.apply {
            apply("org.jetbrains.kotlin.jvm")
            apply("io.gitlab.arturbosch.detekt")
        }
    }

    private fun applyDependencies(project: Project) {
        project.dependencies {
            "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")

            "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

            "testImplementation"("io.kotest:kotest-runner-junit5:5.9.1")
            "testImplementation"("io.kotest:kotest-assertions-core:5.9.1")
            "testImplementation"("io.kotest:kotest-property:5.9.1")
        }
    }

    @Suppress("UnstableApiUsage")
    private fun applyJvmVersions(project: Project) {
        project.extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(JVM_VERSION))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            }
        }

        project.extensions.configure<JavaPluginExtension> {
            val javaVersion = JavaVersion.toVersion(JVM_VERSION)
            if (JavaVersion.current() < javaVersion) {
                toolchain.languageVersion.set(JavaLanguageVersion.of(JVM_VERSION))
                toolchain.vendor.set(JvmVendorSpec.GRAAL_VM)
            }
        }

        project.tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "$JVM_VERSION"
            }
        }
    }

    private fun applyTestConfig(project: Project) {
        project.tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    private fun applyDetekt(project: Project) {
        project.tasks.withType<Detekt> {
            jvmTarget = "$JVM_VERSION"
            reports {
                xml.required.set(true)
                sarif.required.set(true)
            }

            val sarifReportMerge = project.rootProject
                .tasks
                .findByName("sarifReportMerge") as? ReportMergeTask
                ?: run {
                    project.rootProject.apply(plugin = "io.gitlab.arturbosch.detekt")
                    project.rootProject.task<ReportMergeTask>("sarifReportMerge") {
                        output.set(project.rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
                    }
                }
            finalizedBy(sarifReportMerge)
            sarifReportMerge.input.from(sarifReportFile)

            val xmlReportMerge = project.rootProject
                .tasks
                .findByName("xmlReportMerge") as? ReportMergeTask
                ?: run {
                    project.rootProject.apply(plugin = "io.gitlab.arturbosch.detekt")
                    project.rootProject.task<ReportMergeTask>("xmlReportMerge") {
                        output.set(project.rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
                    }
                }
            finalizedBy(xmlReportMerge)
            xmlReportMerge.input.from(xmlReportFile)
        }

        project.extensions.configure<DetektExtension>("detekt") {
            parallel = true
            config = project.rootProject.files("config/detekt/detekt.yml")
            buildUponDefaultConfig = true
        }
    }
}
