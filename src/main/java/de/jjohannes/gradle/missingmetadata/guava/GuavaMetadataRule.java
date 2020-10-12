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
package de.jjohannes.gradle.missingmetadata.guava;

import org.gradle.api.artifacts.CacheableRule;
import org.gradle.api.artifacts.ComponentMetadataContext;
import org.gradle.api.artifacts.ComponentMetadataDetails;
import org.gradle.api.artifacts.ComponentMetadataRule;

import static de.jjohannes.gradle.missingmetadata.guava.GuavaCapabilities.*;
import static org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE;

@CacheableRule
class GuavaMetadataRule implements ComponentMetadataRule {

    public void execute(ComponentMetadataContext ctx) {
        int majorVersion = getMajorVersion(ctx.getDetails());
        int minorVersion = getMinorVersion(ctx.getDetails());
        if (majorVersion > 29 || (majorVersion == 29 && minorVersion > 0)) {
            return;
        }

        fixCapabilities("compile", ctx.getDetails());
        fixCapabilities("runtime", ctx.getDetails());

        removeAnnotationProcessorDependenciesFromRuntime(ctx.getDetails());

        if (majorVersion >= 22) {
            removeAnimalSnifferAnnotations(ctx.getDetails());

            addOtherJvmVariant("Compile", ctx.getDetails());
            addOtherJvmVariant("Runtime", ctx.getDetails());
        }
    }

    private void removeAnimalSnifferAnnotations(ComponentMetadataDetails details) {
        // this is not needed - https://github.com/google/guava/issues/2824
        details.allVariants(variant -> variant.withDependencies(dependencies ->
                dependencies.removeIf(dependency -> "animal-sniffer-annotations".equals(dependency.getName()))));
    }

    private void removeAnnotationProcessorDependenciesFromRuntime(ComponentMetadataDetails details) {
        // everything outside the 'com.google.guava' group is an annotation processor
        details.withVariant("runtime", variant -> variant.withDependencies(dependencies ->
                dependencies.removeIf(dependency -> !CAPABILITY_GUAVA_GROUP.equals(dependency.getGroup()))));
    }

    private void fixCapabilities(String variantName, ComponentMetadataDetails details) {
        String version = getVersion(details);

        details.withVariant(variantName, variant -> {
            variant.withDependencies(dependencies -> dependencies.removeIf(d -> CAPABILITY_LISTENABLEFUTURE_NAME.equals(d.getName())));
            variant.withCapabilities(capabilities -> {
                capabilities.removeCapability(CAPABILITY_GOOGLE_COLLECTIONS_GROUP, CAPABILITY_GOOGLE_COLLECTIONS_NAME);
                capabilities.removeCapability(CAPABILITY_LISTENABLEFUTURE_GROUP, CAPABILITY_LISTENABLEFUTURE_NAME);
                capabilities.addCapability(CAPABILITY_GOOGLE_COLLECTIONS_GROUP, CAPABILITY_GOOGLE_COLLECTIONS_NAME, version);
                capabilities.addCapability(CAPABILITY_LISTENABLEFUTURE_GROUP, CAPABILITY_LISTENABLEFUTURE_NAME, "1.0");
            });
        });
    }

    private boolean isAndroidVariantVersion(ComponentMetadataDetails details) {
        return details.getId().getVersion().endsWith("-android");
    }

    private String getVersion(ComponentMetadataDetails details) {
        String versionString = details.getId().getVersion();
        if (!versionString.contains("-")) {
            return versionString;
        }
        return versionString.substring(0, versionString.indexOf("-"));
    }

    private int getMajorVersion(ComponentMetadataDetails details) {
        String version = getVersion(details);
        return Integer.parseInt(version.substring(0, version.indexOf(".")));
    }

    private int getMinorVersion(ComponentMetadataDetails details) {
        String version = getVersion(details);
        int minorIndex = version.indexOf(".") + 1;
        return Integer.parseInt(version.substring(minorIndex, minorIndex + 1));
    }

    private void addOtherJvmVariant(String baseVariantName, ComponentMetadataDetails details) {
        String version = getVersion(details);
        int majorVersion = getMajorVersion(details);
        int jdkVersion = isAndroidVariantVersion(details) ? 6 : 8;

        String otherJarSuffix = isAndroidVariantVersion(details) ?
                "22.0".equals(version) || "23.0".equals(version) ? "" : "-jre" : "-android";
        int otherJdkVersion = isAndroidVariantVersion(details) ? 8 : 6;

        details.withVariant(baseVariantName.toLowerCase(), variant ->
                variant.attributes(a -> a.attribute(TARGET_JVM_VERSION_ATTRIBUTE, jdkVersion)));
        details.addVariant("jdk" + otherJdkVersion + baseVariantName, baseVariantName.toLowerCase(), variant -> {
            variant.attributes(a -> a.attribute(TARGET_JVM_VERSION_ATTRIBUTE, otherJdkVersion));
            if (majorVersion >= 26 || "25.1".equals(version)) {
                variant.withDependencies(dependencies -> {
                    dependencies.removeIf(d -> "org.checkerframework".equals(d.getGroup()));
                    if (baseVariantName.equals("Compile")) {
                        dependencies.add("org.checkerframework:" + checkerVersionFor(version, !isAndroidVariantVersion(details)));
                    }
                });
            }
            variant.withFiles(files -> {
                files.removeAllFiles();
                files.addFile("guava-" + version + otherJarSuffix + ".jar",
                        "../" + version + otherJarSuffix + "/guava-" + version + otherJarSuffix + ".jar");
            });
        });
    }

    private String checkerVersionFor(String guavaVersion, boolean androidVariant) {
        String name = androidVariant ? "checker-compat-qual" : "checker-qual";
        String version = "";
        if (androidVariant) {
            if (guavaVersion.equals("25.1")) {
                version = "2.0.0";
            } else if (guavaVersion.startsWith("28.") || guavaVersion.startsWith("29.")) {
                version = "2.5.5";
            } else {
                version = "2.5.2";
            }
        } else {
            if (guavaVersion.startsWith("29.")) {
                version = "2.11.1";
            } else if (guavaVersion.equals("28.2")) {
                version = "2.10.0";
            } else if (guavaVersion.startsWith("28.")) {
                version = "2.8.1";
            } else  if (guavaVersion.startsWith("26.") || guavaVersion.startsWith("27.")) {
                version = "2.5.2";
            } else if (guavaVersion.startsWith("25.")) {
                version = "2.0.0";
            }
        }

        return name + ":" + version;
    }
}