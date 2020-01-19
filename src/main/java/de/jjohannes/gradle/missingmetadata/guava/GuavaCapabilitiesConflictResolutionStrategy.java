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
