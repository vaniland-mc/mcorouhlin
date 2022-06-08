plugins {
    id("land.vani.setup.kotlin") apply false
    id("land.vani.setup.maven") apply false
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

group = "land.vani"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "Mojang repository"
            url = uri("https://libraries.minecraft.net")
        }
    }
}

tasks.koverMergedXmlReport {
    isEnabled = true
    xmlReportFile.set(layout.buildDirectory.file("reports/kover/merged.xml"))
}
