/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.arscreo.activation.AddonRuntime;

import java.util.IdentityHashMap;
import java.util.Map;

/** Replaces the empty placeholder with the bounded run flipbook or static GEO. */
final class ArsCreoRenderer implements BlockRenderer {

    private static final String WHEEL = "ars_creo:starbuncle_wheel";
    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final AddonRuntime runtime;
    private final RendererDataRegistry.Data data;
    private final WheelMeshEmitter emitter;
    private final Map<BlockRendererType, BlockRenderer> stockRenderers =
            new IdentityHashMap<>();

    ArsCreoRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        data = RendererDataRegistry.get(resourcePack);
        emitter = new WheelMeshEmitter(resourcePack, textures, settings);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        try {
            if (!renderWheel(block, target, mapColor)) {
                stock(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            target.getTileModel().reset(start);
            target.initialize(start);
            runtime.inactive("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start);
        }
    }

    private boolean renderWheel(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (!runtime.active() || data == null
                || !WHEEL.equals(block.getBlockState().getId().getFormatted())) {
            return false;
        }
        String facing = block.getBlockState().getProperties().get("facing");
        return facing != null && emitter.emit(data, facing, block, target, mapColor);
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = data == null
                    ? BlockRendererType.DEFAULT : data.variants().original(variant);
            stockRenderers.computeIfAbsent(
                    type, found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (RuntimeException exception) {
            target.getTileModel().reset(start);
            target.initialize(start);
            runtime.inactive("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }
}
