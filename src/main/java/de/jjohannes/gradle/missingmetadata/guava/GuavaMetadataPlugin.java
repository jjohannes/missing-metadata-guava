package de.jjohannes.gradle.missingmetadata.guava;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
@NonNullApi
abstract public class GuavaMetadataPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (majorGradleVersion(project) < 6) {
            throw new RuntimeException("This plugin requires Gradle 6+");
        }
        project.getDependencies().getComponents().withModule("com.google.guava:guava", GuavaMetadataRule.class);
        project.getConfigurations().all(c -> c.resolutionStrategy(new GuavaCapabilitiesConflictResolutionStrategy()));
    }

    private int majorGradleVersion(Project project) {
        String version = project.getGradle().getGradleVersion();
        return Integer.parseInt(version.substring(0, version.indexOf(".")));
    }
}