package de.jjohannes.gradle.missingmetadata.guava;

import org.gradle.api.Action;
import org.gradle.api.artifacts.CapabilityResolutionDetails;
import org.gradle.api.artifacts.ComponentVariantIdentifier;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;

import java.util.Set;
import java.util.stream.Collectors;

import static de.jjohannes.gradle.missingmetadata.guava.GuavaCapabilities.*;

public class GuavaCapabilitiesConflictResolutionStrategy implements Action<ResolutionStrategy> {

    @Override
    public void execute(ResolutionStrategy resolutionStrategy) {
        resolutionStrategy.capabilitiesResolution(capabilitiesResolution -> {
            capabilitiesResolution.withCapability(
                    CAPABILITY_GOOGLE_COLLECTIONS_GROUP, CAPABILITY_GOOGLE_COLLECTIONS_NAME,
                    GuavaCapabilitiesConflictResolutionStrategy::selectGuava);
            capabilitiesResolution.withCapability(
                    CAPABILITY_LISTENABLEFUTURE_GROUP, CAPABILITY_LISTENABLEFUTURE_NAME,
                    GuavaCapabilitiesConflictResolutionStrategy::selectGuava);
        });
    }

    private static void selectGuava(CapabilityResolutionDetails details) {
        Set<ComponentVariantIdentifier> guava = details.getCandidates().stream().filter(id ->
                ((ModuleComponentIdentifier) id.getId()).getModule().equals("guava")).collect(Collectors.toSet());
        if (guava.size() == 1) {
            details.select(guava.iterator().next());
        }
    }
}
