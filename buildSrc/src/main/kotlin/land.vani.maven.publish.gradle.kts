import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

val sourceSets = extensions.getByType<SourceSetContainer>()

tasks {
    val javadoc = getByName<Javadoc>("javadoc")
    getByName<DokkaTask>("dokkaJavadoc") {
        outputDirectory.set(javadoc.destinationDir)
    }

    register<Jar>("javadocJar") {
        dependsOn("dokkaJavadoc")

        archiveClassifier.set("javadoc")
        from(javadoc.destinationDir)
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
}

val publishing = extensions.getByName<PublishingExtension>("publishing").apply {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            groupId = "land.vani.mcorouhlin"
            artifactId = project.name
            version = property("mcorouhlin.version").toString()
            pom {
                name.set(project.name)
                description.set(
                    "mcorouhlin is a framework to support the development of plugins for Paper, etc."
                )
                url.set("https://github.com/vaniland-mc/mcorouhlin")
                licenses {
                    license {
                        name.set("GPL v3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("rona_tombo")
                        name.set("rona_tombo")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/vaniland-mc/mcorouhlin.git")
                    developerConnection.set("scm:git:ssh://github.com:vaniland-mc/mcorouhlin.git")
                    url.set("https://github.com/vaniland-mc/mcorouhlin")
                }
            }

            repositories {
                maven {
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    name = "OSSRH"
                    credentials {
                        username = System.getenv("OSSRH_USERNAME")
                        password = System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
        }
    }
}

extensions.configure<SigningExtension>("signing") {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_SECRET_KEY"),
        System.getenv("SIGNING_PASSWORD"),
    )
    sign(publishing.publications["maven"])
}
