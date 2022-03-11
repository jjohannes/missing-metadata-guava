package de.jjohannes.gradle.missingmetadata.guava

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CapabilityConflictResolutionTest extends Specification {

    @Rule
    TemporaryFolder testFolder = new TemporaryFolder()

    def setup() {
        testFolder.newFile("settings.gradle.kts") << """
            pluginManagement {
                repositories.mavenCentral()
                repositories.google()
            }
            rootProject.name = "test-project"
        """
        testFolder.newFile("gradle.properties") << "android.useAndroidX=true"
    }

    def "resolves capability conflicts to Guava (standard-jvm)"() {
        given:
        testFolder.newFile("build.gradle.kts") << """
            plugins {
                id("de.jjohannes.missing-metadata-guava")
                id("java-library")
            }
            
            repositories {
                mavenCentral()
                google()
            }
            
            dependencies {
                implementation("com.google.guava:guava:31.1-jre")
                implementation("com.google.guava:guava:27.1-android") 
                implementation("com.google.collections:google-collections:1.0")
                implementation("com.google.guava:listenablefuture:1.0") 
            }
            
            tasks.register("printJars") {
                println(configurations.compileClasspath.get().files.joinToString("\\n") { it.name });
            }
        """

        expect:
        printJarsGradle7() == [
                "guava-31.1-jre.jar",
                "failureaccess-1.0.1.jar",
                "jsr305-3.0.2.jar",
                "checker-qual-3.12.0.jar",
                "error_prone_annotations-2.11.0.jar",
                "j2objc-annotations-1.3.jar"
        ]
    }

    def "resolves capability conflicts to Guava (android)"() {
        given:
        testFolder.newFile("build.gradle.kts") << """
            plugins {
                id("de.jjohannes.missing-metadata-guava")
                id("com.android.library") version "7.0.4"
            }
            
            repositories {
                mavenCentral()
                google()
            }

            android {
                compileSdk = 30
            }
            
            dependencies {
                implementation("com.google.guava:guava:28.0-jre")
                implementation("com.google.collections:google-collections:1.0")
                implementation("com.google.guava:listenablefuture:1.0") 
                implementation("com.google.android.exoplayer:exoplayer-common:2.13.3")
                implementation("org.robolectric:utils:4.7.2")
            }
            
            tasks.register("printJars") {
                println(configurations.getByName("debugRuntimeClasspath").files.joinToString("\\n") { it.name });
            }
        """

        expect:
        printJarsGradle7() == [
                "exoplayer-common-2.13.3.aar",
                "utils-4.7.2.jar",
                "guava-28.0-android.jar",
                "annotation-1.1.0.jar",
                "error_prone_annotations-2.9.0.jar",
                "pluginapi-4.7.2.jar",
                "annotations-4.7.2.jar",
                "javax.inject-1.jar",
                "javax.annotation-api-1.3.2.jar",
                "failureaccess-1.0.1.jar"
        ]
    }

    List<String> printJarsGradle7() {
        gradleRunnerFor(['printJars']).withGradleVersion("7.3.3").build().output.trim().split("\n").toList()
    }

    GradleRunner gradleRunnerFor(List<String> args) {
        GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(testFolder.root)
                .withArguments(args + '-s' + '-q')
    }
}
