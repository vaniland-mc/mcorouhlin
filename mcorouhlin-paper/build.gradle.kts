plugins {
    id("land.vani.setup.kotlin")
    id("land.vani.setup.maven")
    id("io.papermc.paperweight.userdev") version "1.5.1"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
        }
    }
}

dependencies {
    api(project(":mcorouhlin-api"))
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-extra-kotlin:4.12.0") {
        exclude("net.kyori")
    }

    testImplementation(kotlin("reflect"))
    testImplementation("com.google.jimfs:jimfs:1.2")
}

tasks {
    assemble {
        dependsOn("reobfJar")
    }
}
