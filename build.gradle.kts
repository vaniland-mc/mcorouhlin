plugins {
    id("land.vani.setup.kotlin") apply false
    id("land.vani.setup.maven") apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
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

koverMerged {
    enable()
    xmlReport {
        reportFile.set(layout.buildDirectory.file("reports/kover/merged.xml"))
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}
