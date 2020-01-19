/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    `java-gradle-plugin`
    groovy
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.github.hierynomus.license") version "0.15.0"
}

repositories {
    jcenter()
}

group = "de.jjohannes.gradle"
version = "0.1"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val functionalTestSourceSet = sourceSets.create("functionalTest") { }
gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}
val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
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

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

license {
    header = rootProject.file("config/HEADER.txt")
    strictCheck = true
    ignoreFailures = false
    mapping(mapOf(
        "java"   to "SLASHSTAR_STYLE",
        "kt"     to "SLASHSTAR_STYLE",
        "groovy" to "SLASHSTAR_STYLE",
        "kts"    to "SLASHSTAR_STYLE"
    ))
    ext.set("year", "2020")
    exclude("**/build/*")
    exclude("**/.gradle/*")
}

