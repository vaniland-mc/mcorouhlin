plugins {
    kotlin("jvm")
    id("land.vani.setup.kotlin")
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://papermc.io/repo/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
        }
    }
}

dependencies {
    api(project(":mcorouhlin-api"))
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:dev-bundle:1.18.1-R0.1-SNAPSHOT")
}
