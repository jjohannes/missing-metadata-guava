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
    `maven-publish`
    groovy
    id("com.gradle.plugin-publish") version "0.12.0"
    id("com.github.hierynomus.license") version "0.15.0"
}

repositories {
    jcenter()
}

group = "de.jjohannes.gradle"
version = "0.4"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val functionalTest: SourceSet by sourceSets.creating
gradlePlugin.testSourceSets(functionalTest)
val functionalTestTask = tasks.register<Test>("functionalTest") {
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
