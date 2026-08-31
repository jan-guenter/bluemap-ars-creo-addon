/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoModel;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Classloader-local compiled data keyed by the BlueMap resource pack. */
final class RendererDataRegistry {

    private static final Map<ResourcePack, Data> DATA = new IdentityHashMap<>();

    private RendererDataRegistry() {
    }

    static synchronized void install(
            ResourcePack pack,
            InstalledGeoModel baseModel,
            List<InstalledGeoModel> runPoses,
            List<Key> runTextures,
            VariantRendererCatalog variants
    ) {
        DATA.put(pack, new Data(baseModel, runPoses, runTextures, variants));
    }

    static synchronized Data get(ResourcePack pack) {
        return DATA.get(pack);
    }

    record Data(
            InstalledGeoModel baseModel,
            List<InstalledGeoModel> runPoses,
            List<Key> runTextures,
            VariantRendererCatalog variants
    ) {

        Data {
            runPoses = List.copyOf(runPoses);
            runTextures = List.copyOf(runTextures);
            if (runPoses.size() != runTextures.size()) {
                throw new IllegalArgumentException("run pose and texture counts differ");
            }
        }

        boolean animated() {
            return runPoses.size() == InstalledRunAnimationCompiler.POSE_COUNT;
        }
    }
}
