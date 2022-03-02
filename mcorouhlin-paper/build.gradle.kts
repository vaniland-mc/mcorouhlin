plugins {
    id("land.vani.setup.kotlin")
    id("land.vani.maven.publish")
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

    testImplementation("land.vani.mockpaper:MockPaper-1.18.1:1.0.1")
    testCompileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
}
