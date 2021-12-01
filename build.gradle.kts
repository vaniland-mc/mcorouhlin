plugins {
    kotlin("jvm") version "1.6.0"
}

group = "land.vani"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")

    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    testImplementation("io.kotest:kotest-runner-junit5:5.0.0")
    testImplementation("io.kotest:kotest-assertions-core:5.0.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.17:1.10.4")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
