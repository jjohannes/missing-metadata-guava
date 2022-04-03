package de.jjohannes.gradle.missingmetadata.guava;

import org.gradle.api.Action;
import org.gradle.api.artifacts.CapabilityResolutionDetails;
import org.gradle.api.artifacts.ComponentVariantIdentifier;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;

import java.util.List;
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
            capabilitiesResolution.withCapability(
                    CAPABILITY_GUAVA_GROUP, CAPABILITY_GUAVA_NAME,
                    GuavaCapabilitiesConflictResolutionStrategy::selectGuava);
        });
    }

    private static void selectGuava(CapabilityResolutionDetails details) {
        List<ComponentVariantIdentifier> guava = details.getCandidates().stream().filter(id ->
                ((ModuleComponentIdentifier) id.getId()).getModule().equals("guava")).collect(Collectors.toList());
        if (guava.size() == 1) {
            details.select(guava.get(0));
        } else if (guava.size() > 1) {
            // If there are more than one 'Guava' in the conflict, select the highest version.
            // It is unclear how this happens, because usually the highest version of a component should have been selected before.
            // See: https://github.com/jjohannes/missing-metadata-guava/issues/4
            details.select(findHighestVersionCandidate(guava));
        }
    }

    private static ComponentVariantIdentifier findHighestVersionCandidate(List<ComponentVariantIdentifier> candidates) {
        ComponentVariantIdentifier selected = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            ComponentVariantIdentifier next = candidates.get(i);
            if (next.getId() instanceof ModuleComponentIdentifier) {
                String selectedVersion = ((ModuleComponentIdentifier) selected.getId()).getVersion();
                String nextVersion = ((ModuleComponentIdentifier) next.getId()).getVersion();

                if (nextVersion.compareTo(selectedVersion) > 0) {
                    selected = next;
                }
            }
        }
        return selected;
    }
}
