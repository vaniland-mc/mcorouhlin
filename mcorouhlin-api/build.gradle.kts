plugins {
    id("land.vani.setup.kotlin")
    id("land.vani.maven.publish")
}

dependencies {
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    api("org.jetbrains:annotations:23.0.0")

    api("com.mojang:brigadier:1.0.18")

    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")
}
