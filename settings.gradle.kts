rootProject.name = "mcorouhlin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

include(
    "mcorouhlin-api",
    "mcorouhlin-paper",
)

includeBuild("internal-plugins")
