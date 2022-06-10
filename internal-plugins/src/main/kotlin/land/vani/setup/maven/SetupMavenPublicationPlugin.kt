package land.vani.setup.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask

@Suppress("unused")
class SetupMavenPublicationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        applyPlugins(project)
        setupTasks(project)
        setupPublishing(project)
    }

    private fun applyPlugins(project: Project) {
        project.plugins.run {
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
            apply("org.jetbrains.dokka")
        }
    }

    private fun setupTasks(project: Project) {
        project.tasks.apply {
            setupJavadocTask(project)

            register<Jar>("sourcesJar") {
                archiveClassifier.set("sources")

                val sourceSets = project.extensions.getByType<SourceSetContainer>()
                from(sourceSets["main"].allSource)
            }
        }
    }

    private fun setupJavadocTask(project: Project) {
        project.tasks {
            val javadoc = findByName("javadoc") as? Javadoc ?: return@tasks

            val dokkaJavadoc = findByName("dokkaJavadoc") as? DokkaTask ?: return@tasks

            register<Jar>("javadocJar") {
                dependsOn(dokkaJavadoc)

                archiveClassifier.set("javadoc")
                from(javadoc.destinationDir)
            }
        }
    }

    private fun setupPublishing(project: Project) {
        val publishing = project.extensions.getByName<PublishingExtension>("publishing").apply {
            publications {
                create<MavenPublication>("maven") {
                    from(project.components["kotlin"])
                    artifact(project.tasks["sourcesJar"])
                    artifact(project.tasks["javadocJar"])

                    groupId = "land.vani.mcorouhlin"
                    artifactId = project.name
                    version = project.property("mcorouhlin.version").toString()
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
                }
            }
        }

        project.extensions.configure<SigningExtension>("signing") {
            useInMemoryPgpKeys(
                System.getenv("SIGNING_KEY_ID"),
                System.getenv("SIGNING_SECRET_KEY"),
                System.getenv("SIGNING_PASSWORD"),
            )
            sign(publishing.publications["maven"])
        }
    }
}
