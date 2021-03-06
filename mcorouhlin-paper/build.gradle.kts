plugins {
    id("land.vani.setup.kotlin")
    id("land.vani.setup.maven")
    id("io.papermc.paperweight.userdev") version "1.3.8"
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
    paperDevBundle("1.19.1-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-extra-kotlin:4.11.0") {
        exclude("net.kyori")
    }

    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.106.0") {
        exclude("io.papermc.paper", "paper-api")
    }
    testImplementation(kotlin("reflect"))
    testImplementation("com.google.jimfs:jimfs:1.2")
}

tasks {
    assemble {
        dependsOn("reobfJar")
    }
}
