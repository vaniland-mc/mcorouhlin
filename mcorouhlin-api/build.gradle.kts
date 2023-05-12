plugins {
    id("land.vani.setup.kotlin")
    id("land.vani.setup.maven")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    api("org.jetbrains:annotations:24.0.1")

    api("com.mojang:brigadier:1.0.18")

    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}
