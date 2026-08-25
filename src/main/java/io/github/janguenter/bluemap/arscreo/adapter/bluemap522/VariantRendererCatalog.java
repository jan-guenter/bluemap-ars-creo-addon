/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps the original wheel renderer for atomic stock fallback. */
final class VariantRendererCatalog {

    private static final Key WHEEL = Key.parse("ars_creo:starbuncle_wheel");
    private final Map<Variant, BlockRendererType> originals;

    private VariantRendererCatalog(Map<Variant, BlockRendererType> originals) {
        this.originals = Collections.unmodifiableMap(originals);
    }

    static VariantRendererCatalog wrap(ResourcePack pack, BlockRendererType wrapper) {
        BlockState state = pack.getBlockStates().get(WHEEL);
        if (state == null) {
            throw new IllegalArgumentException("installed wheel blockstate is missing");
        }
        IdentityHashMap<Variant, BlockRendererType> originals = new IdentityHashMap<>();
        state.forEach(variant -> {
            if (variant.getRenderer() != wrapper) {
                originals.put(variant, variant.getRenderer());
                variant.setRenderer(wrapper);
            }
        });
        if (originals.isEmpty()) {
            throw new IllegalArgumentException("installed wheel has no variants");
        }
        return new VariantRendererCatalog(originals);
    }

    BlockRendererType original(Variant variant) {
        return originals.getOrDefault(variant, BlockRendererType.DEFAULT);
    }

    int size() {
        return originals.size();
    }
}
