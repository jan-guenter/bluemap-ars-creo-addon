/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.arscreo.model.WheelModel;

import java.util.IdentityHashMap;
import java.util.Map;

/** Classloader-local compiled data keyed by the BlueMap resource pack. */
final class RendererDataRegistry {

    private static final Map<ResourcePack, Data> DATA = new IdentityHashMap<>();

    private RendererDataRegistry() {
    }

    static synchronized void install(
            ResourcePack pack,
            WheelModel model,
            VariantRendererCatalog variants
    ) {
        DATA.put(pack, new Data(model, variants));
    }

    static synchronized Data get(ResourcePack pack) {
        return DATA.get(pack);
    }

    record Data(WheelModel model, VariantRendererCatalog variants) {
    }
}
