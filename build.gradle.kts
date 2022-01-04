plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("groovy")
    id("com.gradle.plugin-publish") version "0.18.0"
}

repositories {
    mavenCentral()
}

group = "de.jjohannes.gradle"
version = "0.5"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val functionalTest = sourceSets.create("functionalTest")
gradlePlugin.testSourceSets(functionalTest)
val functionalTestTask = tasks.register<Test>(functionalTest.name) {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
}
tasks.check {
    dependsOn(functionalTestTask)
}

dependencies {
    implementation(gradleApi())
    "functionalTestImplementation"("org.spockframework:spock-core:1.2-groovy-2.5")
}

gradlePlugin {
    plugins {
        create("missing-metadata-guava") {
            id = "de.jjohannes.missing-metadata-guava"
            implementationClass = "de.jjohannes.gradle.missingmetadata.guava.GuavaMetadataPlugin"
            displayName = "Additional metadata for Guava"
            description = "Provides additional metadata for Guava versions that were not published with Gradle Module Metadata"
        }
    }
}

pluginBundle {
    website = "https://github.com/jjohannes/missing-metadata-guava"
    vcsUrl = "https://github.com/jjohannes/missing-metadata-guava.git"
    tags = listOf("dependency", "dependencies", "dependency-management", "metadata", "guava")
}
