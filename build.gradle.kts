import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

group = "land.vani"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.create<ReportMergeTask>("sarifReportMerge") {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

tasks.create<ReportMergeTask>("xmlReportMerge") {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
}

tasks.koverMergedXmlReport {
    isEnabled = true
    xmlReportFile.set(layout.buildDirectory.file("reports/kover/merged.xml"))
}
