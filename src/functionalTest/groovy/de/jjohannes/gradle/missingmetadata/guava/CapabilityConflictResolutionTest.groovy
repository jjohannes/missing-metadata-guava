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
                implementation("androidx.core:core-ktx:1.7.0")
                implementation("androidx.appcompat:appcompat:1.4.1")
                implementation("com.google.android.material:material:1.5.0")
                implementation("androidx.browser:browser:1.3.0")
                implementation("com.google.android.exoplayer:exoplayer:2.13.3")
                testImplementation ("org.robolectric:robolectric:4.7.2")
                testImplementation ("junit:junit:4.13.2")
            }
            
            tasks.register("printJars") {
                println(configurations.getByName("debugUnitTestRuntimeClasspath").incoming.artifactView { 
                        attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
                    }.files.joinToString("\\n") { it.name });
            }
        """

        expect:
        printJarsGradle7() == [
                "classes.jar",
                "robolectric-4.7.2.jar",
                "junit-4.13.2.jar",
                "core-ktx-1.7.0-runtime.jar",
                "material-1.5.0-runtime.jar",
                "constraintlayout-2.0.1-runtime.jar",
                "appcompat-1.4.1-runtime.jar",
                "browser-1.3.0-runtime.jar",
                "exoplayer-2.13.3-runtime.jar",
                "shadows-framework-4.7.2.jar",
                "monitor-1.4.0-runtime.jar",
                "junit-4.7.2.jar",
                "resources-4.7.2.jar",
                "sandbox-4.7.2.jar",
                "utils-reflector-4.7.2.jar",
                "plugins-maven-dependency-resolver-4.7.2.jar",
                "shadowapi-4.7.2.jar",
                "utils-4.7.2.jar",
                "pluginapi-4.7.2.jar",
                "annotations-4.7.2.jar",
                "javax.inject-1.jar",
                "javax.annotation-api-1.3.2.jar",
                "bcprov-jdk15on-1.68.jar",
                "hamcrest-core-1.3.jar",
                "kotlin-stdlib-1.5.31.jar",
                "viewpager2-1.0.0-runtime.jar",
                "fragment-1.3.6-runtime.jar",
                "activity-1.2.4-runtime.jar",
                "appcompat-resources-1.4.1-runtime.jar",
                "emoji2-views-helper-1.0.0-runtime.jar",
                "emoji2-1.0.0-runtime.jar",
                "drawerlayout-1.1.1-runtime.jar",
                "coordinatorlayout-1.1.0-runtime.jar",
                "dynamicanimation-1.0.0-runtime.jar",
                "transition-1.2.0-runtime.jar",
                "vectordrawable-animated-1.1.0-runtime.jar",
                "vectordrawable-1.1.0-runtime.jar",
                "viewpager-1.0.0-runtime.jar",
                "legacy-support-core-utils-1.0.0-runtime.jar",
                "loader-1.0.0-runtime.jar",
                "exoplayer-ui-2.13.3-runtime.jar",
                "recyclerview-1.1.0-runtime.jar",
                "customview-1.1.0-runtime.jar",
                "media-1.2.1-runtime.jar",
                "core-1.7.0-runtime.jar",
                "cursoradapter-1.0.0-runtime.jar",
                "lifecycle-viewmodel-savedstate-2.3.1-runtime.jar",
                "savedstate-1.1.0-runtime.jar",
                "lifecycle-viewmodel-2.3.1-runtime.jar",
                "resourceinspection-annotation-1.0.0.jar",
                "cardview-1.0.0-runtime.jar",
                "versionedparcelable-1.1.1-runtime.jar",
                "collection-1.1.0.jar",
                "concurrent-futures-1.0.0.jar",
                "interpolator-1.0.0-runtime.jar",
                "exoplayer-dash-2.13.3-runtime.jar",
                "exoplayer-hls-2.13.3-runtime.jar",
                "exoplayer-smoothstreaming-2.13.3-runtime.jar",
                "exoplayer-transformer-2.13.3-runtime.jar",
                "exoplayer-core-2.13.3-runtime.jar",
                "lifecycle-process-2.4.0-runtime.jar",
                "startup-runtime-1.0.0-runtime.jar",
                "tracing-1.0.0-runtime.jar",
                "lifecycle-livedata-2.0.0-runtime.jar",
                "lifecycle-livedata-core-2.3.1-runtime.jar",
                "lifecycle-runtime-2.4.0-runtime.jar",
                "core-runtime-2.1.0-runtime.jar",
                "core-common-2.1.0.jar",
                "exoplayer-extractor-2.13.3-runtime.jar",
                "exoplayer-common-2.13.3-runtime.jar",
                "documentfile-1.0.0-runtime.jar",
                "localbroadcastmanager-1.0.0-runtime.jar",
                "print-1.0.0-runtime.jar",
                "lifecycle-common-2.4.0.jar",
                "annotation-1.3.0.jar",
                "annotation-experimental-1.1.0-runtime.jar",
                "nativeruntime-4.7.2.jar",
                "guava-27.1-android.jar",
                "asm-commons-9.2.jar",
                "asm-util-9.2.jar",
                "asm-analysis-9.2.jar",
                "asm-tree-9.2.jar",
                "asm-9.2.jar",
                "error_prone_annotations-2.9.0.jar",
                "sqlite4java-1.0.392.jar",
                "icu4j-53.1.jar",
                "auto-value-annotations-1.7.4.jar",
                "annotations-13.0.jar",
                "kotlin-stdlib-common-1.5.31.jar",
                "constraintlayout-solver-2.0.1.jar",
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
